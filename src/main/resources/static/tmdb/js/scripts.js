// Constants and Configurations
const API_BASE_URL = 'http://localhost:7010/api/tmdb';
const IMAGE_BASE_URL = 'https://image.tmdb.org/t/p';
const DEFAULT_IMAGE = 'https://via.placeholder.com/220x330?text=No+Image';

// Cache DOM elements
const domElements = {
    detailModal: () => document.getElementById('detailModal'),
    providersModal: () => document.getElementById('providersModal'),
    loadingModal: () => document.getElementById('loadingModal'),
    contentList: () => document.getElementById('content-list'),
    pagination: () => document.getElementById('pagination'),
    pageTitle: () => document.getElementById('page-title'),
    filterResetButton: () => document.getElementById('filterResetButton'),
    filterApplyButton: () => document.getElementById('filterApplyButton'),
    startDate: () => document.getElementById('startDate'),
    endDate: () => document.getElementById('endDate'),
    sortFilter: () => document.getElementById('sortFilter'),
    providersList: () => document.getElementById('providers-list')
};

// Utility Functions    
const utils = {
    createUrl: (base, path, params = {}) => {
        const url = new URL(`${base}${path}`);
        Object.entries(params).forEach(([key, value]) => {
            if (value != null) url.searchParams.append(key, value);
        });
        return url;
    },

    async fetchWithRetry(url, options = {}, retries = 3) {
        try {
            const response = await fetch(url, options);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return await response.json();
        } catch (error) {
            throw error;
        }
    },

    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    getContentType() {
        return domElements.pageTitle().textContent.includes('Películas') ? 'movies' : 'tv';
    },

    capitalize(str) {
        const translations = {
            'trending': 'Tendencias',
            'favorites': 'Favoritos',
            'popular': 'Populares'
        };
        return translations[str] || str.charAt(0).toUpperCase() + str.slice(1).replace('_', ' ');
    },

    checkFilters() {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const hasProviders = appState.state.providers.operatorFilter.length > 0;
        const hasActiveFilters = startDate || endDate || hasProviders;
        document.getElementById('filterApplyButton').disabled = !hasActiveFilters;
        console.log('[checkFilters] Checked filters, hasActiveFilters:', hasActiveFilters);
    }
};

// API Service
const apiService = {
    async fetch(endpoint, options = {}) {
        const url = utils.createUrl(API_BASE_URL, endpoint);
        console.log(`[fetch] Fetching data from URL: ${url}`);
        const response = await fetch(url);
        if (!response.ok) {
            console.error(`[fetch] Failed to fetch data from ${url}`);
            throw new Error(`Error fetching data from ${url}`);
        }
        console.log(`[fetchD] Successfully fetched data from ${url}`);
        return response.json();
    },

    async post(endpoint, data) {
        const url = utils.createUrl(API_BASE_URL, endpoint);
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
    }
};

// Content Service
const contentService = {
    init() {
        document.getElementById('searchForm').addEventListener('submit', async (event) => {
            event.preventDefault(); // Evita el recargo de la página
            const query = document.getElementById('searchInput').value.trim(); // Obtén el texto ingresado

            if (query) {
                try {
                    await this.searchByTitle(query); // Inicia búsqueda desde la página 1
                } catch (error) {
                    console.error('Error during search:', error);
                }
            }
        });
    },

    async loadContent(category, type, filters = {}, sort = 'popularity.desc', page = 1) {
        try {
            filtersManager.enableFilters();

            const params = new URLSearchParams();
            let url = `/discover/${type}`;

            if (category === 'favorites') {
                url = `/favorites/${type}`;
            } else if (category === 'trending') {
                url = `/trending/${type}`;
            }

            if (sort) params.append('sort_by', sort);
            if (filters.start_date) params.append('release_date.gte', filters.start_date);
            if (filters.end_date) params.append('release_date.lte', filters.end_date);
            if (filters.providers) {
                params.append('with_watch_providers', filters.providers);
                params.append('watch_region', 'ES');
            }
            params.append('page', page);

            const data = await apiService.fetch(`${url}?${params.toString()}`);
            appState.updateState('pagination', { currentPage: page, totalPages: data.total_pages });
            renderManager.scheduleRender('content', () => this.renderContent(data.results, type));
            renderManager.scheduleRender('pagination', () => this.renderPagination(data.total_pages, page));
            document.getElementById('page-title').textContent = `${utils.capitalize(category)} ${type === 'movies' ? 'Películas' : 'Series'}`;

        } catch (error) {
            console.error('Error loading content:', error);
            throw error;
        }
    },

    async loadFavorites() {
        try {
            const [movies, series] = await Promise.all([
                apiService.fetch('/favorites/movies?page=1'),
                apiService.fetch('/favorites/tv?page=1')
            ]);

            appState.updateState('favorites.movies', new Set(movies.results.map(item => item.id)));
            appState.updateState('favorites.series', new Set(series.results.map(item => item.id)));
        } catch (error) {
            console.error('Error loading favorites:', error);
            throw error;
        }
    },

    renderContent(items, type) {
        const container = domElements.contentList();
        container.innerHTML = items.map(item => this.createCards(item, type)).join('');
    },

    createCards(item, type) {
        const popularity = Math.round((item.vote_average || 0) * 10);
        const isFavorite = type === 'movies'
            ? appState.state.favorites.movies.has(item.id)
            : appState.state.favorites.series.has(item.id);
        const posterPath = item.poster_path
            ? `${IMAGE_BASE_URL}/w220_and_h330_face${item.poster_path}`
            : DEFAULT_IMAGE;
        const formattedDate = new Date(item.release_date).toLocaleDateString('es-ES', {
            day: '2-digit', month: 'long', year: 'numeric'
        });
        return `
            <div class="col-md-2">
                <div class="card custom-card">
                    <div class="card-img-top position-relative" onclick="contentService.showDetail(${item.id}, '${type}')">
                        <img src="${posterPath}" alt="${item.title || item.name}">
                        <div class="popularity-wrapper">
                            <div class="popularity-graph" style="--value:${popularity};"></div>
                            <span>${popularity}%</span>
                        </div>
                    </div>
                    <div class="content">
                        <p>${formattedDate}</p>
                        <br/>
                    </div>
                    <div class="favorite-icon position-absolute bottom-0 end-0 m-2">
                        <button class="btn btn-sm ${isFavorite ? 'btn-danger' : 'btn-dark'}" 
                            onclick="contentService.toggleFavorite(event, ${item.id}, '${type}')">
                            <i class="fas fa-heart"></i>
                        </button>
                        <!-- Botón de descarga -->
                        <button class="btn btn-sm btn-primary"
                        	onclick="handleDownload('${item.id}', '${type}')">
                            <i class="fas fa-download"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
    },

    renderPagination(totalPages, currentPage, query = null) {
        const paginationContainer = domElements.pagination();
        paginationContainer.innerHTML = '';

        // Botón "Inicio"
        this.addPaginationButton(
            paginationContainer,
            '|<<',
            1,
            currentPage === 1,
            false,
            () => query ? this.changeSearchPage(query, 1) : this.changePage(1)
        );
        // Botón "Anterior"
        this.addPaginationButton(
            paginationContainer,
            'Anterior',
            currentPage - 1,
            currentPage === 1,
            false,
            () => query ? this.changeSearchPage(query, currentPage - 1) : this.changePage(currentPage - 1)
        );

        const { startPage, endPage } = this.calculatePaginationRange(totalPages, currentPage);
        for (let i = startPage; i <= endPage; i++) {
            this.addPaginationButton(
                paginationContainer,
                i,
                i,
                false,
                i === currentPage,
                () => query ? this.changeSearchPage(query, i) : this.changePage(i)
            );
        }

        // Botón "Siguiente"
        this.addPaginationButton(
            paginationContainer,
            'Siguiente',
            currentPage + 1,
            currentPage === totalPages,
            false,
            () => query ? this.changeSearchPage(query, currentPage + 1) : this.changePage(currentPage + 1)
        );

        // Botón "Fin"
        this.addPaginationButton(
            paginationContainer,
            '>>|',
            totalPages,
            currentPage === totalPages,
            false,
            () => query ? this.changeSearchPage(query, totalPages) : this.changePage(totalPages)
        );
    },

    addPaginationButton(container, label, page, disabled, active = false, onClick) {
        const classList = [
            "page-item",
            active ? "active" : "",
            disabled ? "disabled" : ""
        ].join(" ").trim();

        const buttonHTML = `
            <li class="${classList}">
                <button class="page-link">${label}</button>
            </li>
        `;

        const buttonElement = document.createElement('div');
        buttonElement.innerHTML = buttonHTML.trim();
        const buttonNode = buttonElement.firstElementChild;

        // Si no está deshabilitado, añade el evento de clic
        if (!disabled) {
            buttonNode.querySelector('.page-link').addEventListener('click', onClick);
        }

        // Añade el botón al contenedor
        container.appendChild(buttonNode);
    },

    calculatePaginationRange(totalPages, currentPage, maxPagesToShow = 5) {
        const startPage = Math.max(currentPage - Math.floor(maxPagesToShow / 2), 1);
        const endPage = Math.min(startPage + maxPagesToShow - 1, totalPages);
        return { startPage, endPage };
    },

    async showDetail(id, type) {
        try {
            const data = await apiService.fetch(`/${type === 'movies' ? 'movie' : 'tv'}/${id}`);
            const providers = await apiService.fetch(`/${id}/watch/providers/${type === 'movies' ? 'movie' : 'tv'}`);

            renderManager.scheduleRender('detail-modal', () => this.renderDetailModal(data, providers));

            // Make sure the element exists first
            const modal = document.getElementById('detailModal');
            if (modal) {
                const bootstrapModal = new bootstrap.Modal(modal);
                bootstrapModal.show();
            } else {
                console.error('Modal element not found');
            }

        } catch (error) {
            console.error('Error loading detail:', error);
        }
    },

    handleDownload(id, type) {
        if (type === 'movies') {
            apiService.post(`/torrents/download/${id}`)
                .then(() => alert('Descarga de película iniciada'))
                .catch(error => console.error('Error al descargar película:', error));
        } else if (type === 'tv') {
            apiService.fetch(`/series/${id}`)
                .then(data => showSeriesModal(data))
                .catch(error => console.error('Error al cargar detalles de la serie:', error));
        }
    },

    renderDetailModal(data, providers) {
        const posterPath = data.poster_path
            ? `${IMAGE_BASE_URL}/w500${data.poster_path}`
            : DEFAULT_IMAGE;

        domElements.detailModal().querySelector('#detail-image').innerHTML = `
            <img src="${posterPath}" alt="${data.title || data.name}" class="img-fluid" style="max-width: 220px;">
        `;
        domElements.detailModal().querySelector('#detail-title').textContent = data.title || data.name;
        domElements.detailModal().querySelector('#detail-date').textContent =
            (data.release_date || data.first_air_date || '').split('-').reverse().join('-');
        domElements.detailModal().querySelector('#detail-overview').textContent = data.overview;

        const providersContainer = domElements.detailModal().querySelector('#detail-providers .d-flex');
        providersContainer.innerHTML = providers.length
            ? providers.map(provider => `
                <div class="me-3 text-center">
                    <img src="${IMAGE_BASE_URL}/w45${provider.logo_path}" alt="${provider.provider_name}" class="img-fluid mb-1">
                    <p class="small">${provider.provider_name}</p>
                </div>
            `).join('')
            : '<p class="text-muted">No disponible en ninguna operadora.</p>';
    },

    async toggleFavorite(event, id, type) {
        event.stopPropagation();
        try {
            const favorites = type === 'movies'
                ? appState.state.favorites.movies
                : appState.state.favorites.series;
            const isFavorite = favorites.has(id);

            await apiService.post(`/favorites?mediaId=${id}&mediaType=${type === 'movies' ? 'movie' : 'tv'}&favorite=${!isFavorite}`);

            const newFavorites = new Set(favorites);
            if (isFavorite) {
                newFavorites.delete(id);
            } else {
                newFavorites.add(id);
            }

            appState.updateState(`favorites.${type}`, newFavorites);

            const button = event.target.closest('button');
            button.classList.toggle('btn-danger', !isFavorite);
            button.classList.toggle('btn-dark', isFavorite);
        } catch (error) {
            console.error('Error toggling favorite:', error);
        }
    },

    changePage(page) {
        page = page > 500 ? 500 : page;
        appState.updateState('pagination.currentPage', page);
        this.loadContent(
            appState.state.filters.category,
            utils.getContentType(),
            appState.state.filters.applied,
            appState.state.filters.sort,
            page
        );
    },

    async searchByTitle(query, page = 1) {
        try {
            console.log(`[searchByTitle] Searching for: ${query}`);
            const data = await apiService.fetch(`/search?query=${encodeURIComponent(query)}&page=${page}`);

            filtersManager.disableFilters();
            document.getElementById('page-title').textContent = "Resultados de la busqueda";

            if (data.results && data.results.length > 0) {
                console.log(`[searchByTitle] Found ${data.results.length} results`);
                appState.updateState('pagination', { currentPage: page, totalPages: data.total_pages });

                this.renderSearchResults(data.results);
                this.renderPagination(data.total_pages, page, query); // Renderizar paginación específica de la búsqueda
            } else {
                console.log(`[searchByTitle] No results found for: ${query}`);
                domElements.contentList().innerHTML = `<p class="text-center">No se encontraron resultados para "${query}".</p>`;
                domElements.pagination().innerHTML = ''; // Limpiar paginación
            }
        } catch (error) {
            console.error('Error fetching search results:', error);
            domElements.contentList().innerHTML = `<p class="text-center text-danger">Error al realizar la búsqueda.</p>`;
        }
    },

    renderSearchResults(results) {
        const container = domElements.contentList();
        container.innerHTML = results.map(item => this.createSearchResultCard(item)).join('');
    },

    createSearchResultCard(item) {
        const posterPath = item.poster_path
            ? `${IMAGE_BASE_URL}/w220_and_h330_face${item.poster_path}`
            : DEFAULT_IMAGE;
        const releaseDate = item.release_date || item.first_air_date || 'Sin fecha';
        const formattedDate = new Date(releaseDate).toLocaleDateString('es-ES', {
            day: '2-digit', month: 'long', year: 'numeric'
        });

        // Verificar si el elemento ya está marcado como favorito
        const isFavorite = appState.state.favorites.movies.has(item.id) || appState.state.favorites.series.has(item.id);

        return `
        <div class="card mb-3 shadow-sm" style="max-width: 100%;">
            <div class="row g-0 align-items-center">
                <!-- Imagen a la izquierda -->
                <div class="col-md-2">
                    <img src="${posterPath}" class="img-fluid rounded-start search-result-image" alt="${item.title || item.name}">
                </div>
                <!-- Contenido a la derecha -->
                <div class="col-md-9">
                    <div class="card-body">
                        <h5 class="card-title fw-bold">${item.title || item.name}</h5>
                        <p class="card-text text-muted">${formattedDate}</p>
                        <p class="card-text">${item.overview || 'Sin sinopsis disponible'}</p>
                        <!-- Botón de favorito -->
                        <button class="btn btn-sm ${isFavorite ? 'btn-danger' : 'btn-dark'} favorite-btn"
                                onclick="contentService.toggleFavorite(event, ${item.id}, '${item.media_type || 'movie'}')">
                            <i class="fas fa-heart"></i> ${isFavorite ? 'Quitar de Favoritos' : 'Agregar a Favoritos'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
        `;
    },

    async changeSearchPage(query, page) {
        if (page < 1 || page > appState.state.pagination.totalPages) return;

        console.log(`[changeSearchPage] Changing to page: ${page}`);
        await this.searchByTitle(query, page);
    },

    showSeriesModal(seriesData) {
        const modal = document.getElementById("seriesModal");
        document.getElementById("seriesTitle").textContent = seriesData.title;
        document.getElementById("seriesPoster").src = seriesData.posterUrl;
        document.getElementById("seriesReleaseDate").textContent = `Fecha de estreno: ${seriesData.releaseDate}`;

        const seasonsList = document.getElementById("seasonsList");
        seasonsList.innerHTML = "";

        seriesData.seasons.forEach(season => {
            const seasonDiv = document.createElement("div");
            seasonDiv.classList.add("season");

            const seasonTitle = document.createElement("h6");
            seasonTitle.textContent = `Temporada ${season.number}`;
            seasonDiv.appendChild(seasonTitle);

            season.episodes.forEach(episode => {
                const episodeDiv = document.createElement("div");
                episodeDiv.classList.add("episode");
                episodeDiv.innerHTML = `
                    <span>${episode.number}. ${episode.title}</span>
                    <button class="btn btn-sm btn-primary" onclick="downloadEpisode('${episode.id}')">
                        <i class="fas fa-download"></i>
                    </button>
                `;
                seasonDiv.appendChild(episodeDiv);
            });

            seasonsList.appendChild(seasonDiv);
        });

        new bootstrap.Modal(modal).show();
    }


};

// State Management
class AppState {
    constructor() {
        this.state = {
            favorites: { movies: new Set(), series: new Set() },
            providers: { movies: [], series: [], subscribed: new Set() },
            pagination: { currentPage: 1, totalPages: 1 },
            providers: {
                movies: [],
                series: [],
                subscribed: new Set(),
                operatorFilter: [] // Añadido para mantener el estado de los operadores seleccionados
            },
            filters: {
                category: 'popular',
                sort: 'popularity.desc',
                applied: {},
                operators: []
            }
        };
    }

    // Métodos para actualizar el estado de manera inmutable
    updateState(path, value) {
        const newState = { ...this.state };
        let current = newState;
        const parts = path.split('.');
        const last = parts.pop();

        parts.forEach(part => {
            current[part] = { ...current[part] };
            current = current[part];
        });

        current[last] = value;
        this.state = newState;

        // Disparar eventos de actualización si es necesario
        //this.notifyUpdate(path, value);
    }
}

// Inicialización y gestión de eventos
const appState = new AppState();

// Optimización de renderizado
const renderManager = {
    scheduledRenders: new Set(),

    scheduleRender(componentId, renderFn) {
        if (this.scheduledRenders.has(componentId)) return;

        this.scheduledRenders.add(componentId);
        requestAnimationFrame(() => {
            renderFn();
            this.scheduledRenders.delete(componentId);
        });
    }
};

const providerService = {
    async loadProviders() {
        try {
            const [movies, series] = await Promise.all([
                apiService.fetch('/watch/providers/movies'),
                apiService.fetch('/watch/providers/tv')
            ]);
            appState.updateState('providers.movies', movies);
            appState.updateState('providers.series', series);
        } catch (error) {
            console.error('Error loading providers:', error);
            throw error;
        }
    },

    async loadSubscribedProviders() {
        try {
            const subscribed = await apiService.fetch('/providers/subscribe');
            appState.updateState('providers.subscribed', new Set(subscribed.map(p => p.provider_id)));
            renderManager.scheduleRender('providers-list', () => this.renderSubscribedProviders());
        } catch (error) {
            console.error('Error loading subscribed providers:', error);
            throw error;
        }
    },

    async renderSubscribedProviders() {
        console.log("[loadSubscribedProviders] Loading subscribed providers...");
        const container = document.getElementById('providers-list');
        container.innerHTML = '';

        const data = await apiService.fetch(`/providers/subscribe`);
        appState.state.providers.subscribed = new Set(data.map(provider => provider.provider_id));
        console.log("[loadSubscribedProviders] Subscribed providers loaded:", Array.from(appState.state.providers.subscribed));

        data.forEach((provider) => {
            const providerElement = document.createElement('div');
            providerElement.classList.add('provider-item', 'me-2');
            providerElement.innerHTML = `
                <img src="https://image.tmdb.org/t/p/w45${provider.logo_path}" alt="${provider.provider_name}" />
                <span class="provider-name">${provider.provider_name}</span>
            `;
            providerElement.setAttribute("data-provider-id", provider.provider_id);
            providerElement.addEventListener('click', () => this.toggleOperatorSelection(providerElement, provider.provider_id));

            container.appendChild(providerElement);
        });
    },

    toggleOperatorSelection(card, providerId) {
        const providerCards = document.querySelectorAll('.provider-item.selected');

        console.log(`[toggleOperatorSelection] Toggling operator selection for ProviderID: ${providerId}`);
        if (appState.state.providers.operatorFilter.includes(providerId)) {
            appState.state.providers.operatorFilter = appState.state.providers.operatorFilter.filter(id => id !== providerId);
            card.classList.remove('selected');
        } else {
            appState.state.providers.operatorFilter.push(providerId);
            card.classList.add('selected');
        }
        utils.checkFilters();
        console.log('[toggleOperatorSelection] Current operatorFilter:', appState.state.providers.operatorFilter);
    }
};

// Controlador de eventos de providers optimizado
const filtersManager = {
    init() {

        document.getElementById('filterApplyButton').disabled = false;
        document.getElementById('filterResetButton').disabled = true;

        // Initialize filter events
        domElements.filterResetButton().addEventListener('click', this.handleResetFilters);
        domElements.filterApplyButton().addEventListener('click', this.handleApplyFilters);
        domElements.sortFilter().addEventListener('change', this.handleSortChange);

        domElements.providersModal().addEventListener('show.bs.modal', () => {
            const contentType = utils.getContentType();
            renderManager.scheduleRender('providers-modal', () =>
                this.renderProvidersInModal(contentType)
            );
        });

        document.getElementById('save-providers-button')?.addEventListener('click', () => {
            const contentType = utils.getContentType();
            this.saveSelectedProviders(contentType);
        });

        document.querySelectorAll('#startDate, #endDate').forEach(input => {
            input.addEventListener('change', () => {
                const hasFilters = document.getElementById('startDate').value || document.getElementById('endDate').value;
                document.getElementById('filterResetButton').disabled = !hasFilters;
                utils.checkFilters();
                console.log('[change startDate/endDate] Filters changed, hasFilters:', hasFilters);
            });
        });
    },

    renderProvidersInModal(type) {
        console.log(`[renderProvidersInModal] Rendering providers in modal for type: ${type}`);
        const providers = type === 'movies' ? appState.state.providers.movies : appState.state.providers.series;
        const container = document.getElementById('providers-container');
        container.innerHTML = '';

        if (providers.length === 0) {
            container.innerHTML = '<p>No hay proveedores disponibles.</p>';
            console.log('[renderProvidersInModal] No providers available.');
            return;
        }

        providers.forEach((provider) => {
            const isSelected = appState.state.providers.subscribed.has(provider.provider_id);
            const col = document.createElement('div');
            col.className = 'col-md-3 mb-3';
            col.innerHTML = `
                <div class="card provider-card ${isSelected ? 'selected' : ''}" 
                    data-id="${provider.provider_id}">
                    <img src="https://image.tmdb.org/t/p/w92${provider.logo_path}" alt="${provider.provider_name}" class="provider-logo mx-auto mt-3">
                    <div class="card-body text-center">
                        <h6 class="card-title">${provider.provider_name}</h6>
                    </div>
                </div>
            `;
            container.appendChild(col);
            col.addEventListener('click', function () {
                filtersManager.toggleProviderSelection(provider.provider_id);
            });
        });
        console.log('[renderProvidersInModal] Providers rendered in modal.');
    },

    toggleProviderSelection(providerId) {
        console.log(`[toggleProviderSelection] Toggling provider selection for ID: ${providerId}`);
        const card = document.querySelector(`.provider-card[data-id="${providerId}"]`);

        if (appState.state.providers.subscribed.has(providerId)) {
            appState.state.providers.subscribed.delete(providerId);
            card.classList.remove('selected');
        } else {
            appState.state.providers.subscribed.add(providerId);
            card.classList.add('selected');
        }

        console.log('[toggleProviderSelection] Updated subscribed providers:', Array.from(appState.state.providers.subscribed));
    },

    async saveSelectedProviders(type) {
        try {
            const providers = type === 'movies'
                ? appState.state.providers.movies
                : appState.state.providers.series;

            const selectedProviders = providers
                .filter(provider => appState.state.providers.subscribed.has(provider.provider_id))
                .map(({ provider_id, provider_name, logo_path }) => ({
                    provider_id,
                    provider_name,
                    logo_path
                }));

            await apiService.post('/providers/subscribe', selectedProviders);
            renderManager.scheduleRender('providers-list', () =>
                providerService.renderSubscribedProviders()
            );

            bootstrap.Modal.getInstance(domElements.providersModal()).hide();
        } catch (error) {
            console.error('Error saving providers:', error);
        }
    },

    handleProviderSelection(providerId) {
        const subscribed = new Set(appState.state.providers.subscribed);

        if (subscribed.has(providerId)) {
            subscribed.delete(providerId);
        } else {
            subscribed.add(providerId);
        }

        appState.updateState('providers.subscribed', subscribed);

        renderManager.scheduleRender('provider-selection', () => {
            const card = document.querySelector(`.provider-card[data-id="${providerId}"]`);
            if (card) {
                card.classList.toggle('selected', subscribed.has(providerId));
            }
        });
    },

    disableFilters() {
        const filtersColumn = document.querySelector('.filters-column');
        if (filtersColumn) {
            filtersColumn.style.display = 'none';
        }
    },

    enableFilters() {
        const filtersColumn = document.querySelector('.filters-column');
        if (filtersColumn) {
            filtersColumn.style.display = 'block';
        }
    },

    disableProviderItems() {
        const providerItems = document.querySelectorAll('.provider-item');
        providerItems.forEach((item) => {
            item.classList.add('disabled');
            item.removeEventListener('click', this.handleProviderSelection);
        });
    },

    enableProviderItems() {
        const providerItems = document.querySelectorAll('.provider-item');
        providerItems.forEach((item) => {
            item.classList.remove('disabled');
            item.addEventListener('click', this.handleProviderSelection);
        });
    },

    async handleResetFilters() {
        domElements.startDate().value = "";
        domElements.endDate().value = "";

        const providerCards = document.querySelectorAll('.provider-item.selected');
        providerCards.forEach(card => card.classList.remove('selected'));
        domElements.filterApplyButton.disabled = true;
        domElements.filterResetButton.disabled = true;

        // Reset the filter state to default values
        appState.state.filters = {
            applied: {},
            sort: 'default'
        };

        // Reset the pagination
        appState.state.pagination.currentPage = 1;

        // Reload the content with reset filters
        await contentService.loadContent(
            appState.state.filters.category,
            utils.getContentType(),
            appState.state.filters.applied,
            appState.state.filters.sort,
            appState.state.pagination.currentPage
        );
        document.getElementById('filterApplyButton').disabled = true;
        document.getElementById('filterResetButton').disabled = true;
    },

    async handleApplyFilters() {
        // Asumiendo que tienes un método para obtener los filtros actuales del DOM
        const startDate = domElements.startDate().value;
        const endDate = domElements.endDate().value;
        const selectedProviders = appState.state.providers.operatorFilter;

        appState.state.filters.applied = {
            start_date: startDate || null,
            end_date: endDate || null,
            providers: selectedProviders.length > 0 ? Array.from(selectedProviders).join('|') : null
        };

        // Reset pagination cuando se aplican nuevos filtros
        appState.state.pagination.currentPage = 1;

        // Recargar el contenido con los nuevos filtros
        await contentService.loadContent(
            appState.state.filters.category,
            'movies',
            appState.state.filters.applied,
            appState.state.filters.sort,
            appState.state.pagination.currentPage
        );
        document.getElementById('filterResetButton').disabled = false;
    },

    async handleSortChange(event) {
        // Obtener el nuevo valor de ordenamiento del evento
        const newSortValue = event.target.value;

        // Actualizar el estado de ordenamiento
        appState.state.filters.sort = newSortValue;

        // Reset pagination cuando se cambia el ordenamiento
        appState.state.pagination.currentPage = 1;

        // Recargar el contenido con el nuevo ordenamiento
        await contentService.loadContent(
            appState.state.filters.category,
            'movies',
            appState.state.filters.applied,
            appState.state.filters.sort,
            appState.state.pagination.currentPage
        );
    }

};

// Modified Loading Modal Management
const loadingModal = {
    modal: null,
    // Utility Functions for Modal Accessibility
    setModalAccessibility(modalElement, isVisible) {
        const mainContent = document.querySelector('.container-fluid'); // Usar otro contenedor global

        if (!mainContent || !modalElement) {
            console.warn('Main content or modal element is missing. Skipping accessibility settings.');
            return;
        }

        if (isVisible) {
            mainContent.setAttribute('aria-hidden', 'true');
            mainContent.setAttribute('inert', '');

            modalElement.removeAttribute('aria-hidden');
            modalElement.removeAttribute('inert');
            modalElement.focus();
        } else {
            mainContent.removeAttribute('aria-hidden');
            mainContent.removeAttribute('inert');

            modalElement.setAttribute('aria-hidden', 'true');
            modalElement.setAttribute('inert', '');
        }
    },
    show() {
        if (!this.modal) {
            this.modal = new bootstrap.Modal(domElements.loadingModal());
        }
        setModalAccessibility(domElements.loadingModal(), true); // Configuración de accesibilidad
        this.modal.show();
    },

    hide() {
        if (this.modal) {
            this.modal.hide();
            setModalAccessibility(domElements.loadingModal(), false); // Restaurar accesibilidad
            this.modal = null;
        }
    }
};

// Initialize Application
async function initializeApp() {
    try {
        contentService.init();
        //loadingModal.show();
        await Promise.all([
            contentService.loadFavorites(),
            providerService.loadProviders(),
            providerService.loadSubscribedProviders()
        ]);
        filtersManager.init();
        await contentService.loadContent(
            appState.state.filters.category,
            'movies',
            appState.state.filters.applied,
            appState.state.filters.sort,
            appState.state.pagination.currentPage
        );
        //loadingModal.hide();
    } catch (error) {
        console.error('Error in initialization:', error);
        //loadingModal.hide();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    try {
        initializeApp();
        console.log(domElements.loadingModal());
    } catch (error) {
        console.error('Error during initialization:', error);
    }
});




