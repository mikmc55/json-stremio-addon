
# Stremio Addon - Torrent Searcher

Este proyecto es un addon para [Stremio](https://www.stremio.com/) desarrollado en Spring Boot que permite buscar contenido en diferentes proveedores de torrents. La aplicación está dockerizada para facilitar su despliegue y utiliza un archivo de configuración externo para los buscadores de torrents.

## Características

- Búsqueda de torrents en múltiples proveedores.
- Configuración dinámica de los buscadores a través de un archivo de configuración.
- Soporte para despliegue en contenedores Docker.

## Requisitos previos

1. [Docker](https://www.docker.com/get-started) instalado en el sistema.
2. [Java 17](https://openjdk.java.net/projects/jdk/17/) (para desarrollo sin Docker).
3. [Maven](https://maven.apache.org/) (opcional si prefieres compilar manualmente sin Docker).

## Instalación

### Paso 1: Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/stremio-torrent-addon.git
cd stremio-torrent-addon
```

### Paso 2: Configurar el archivo de configuración de los buscadores

Dentro del proyecto, crea un archivo de configuración JSON con la información de los buscadores de torrents:

```json
[
  {
    "id": 1,
    "name": "MejorTorrent",
    "type": "Movie",
    "url": "https://example.com"
  },
  {
    "id": 2,
    "name": "EliteTorrent",
    "type": "Series",
    "url": "https://example.com"
  }
]
```

Guarda este archivo en la ubicación que prefieras, por ejemplo: `./config/torrent_searchers.json`.

### Paso 3: Dockerizar y ejecutar

#### Construcción de la imagen Docker

```bash
docker build -t stremio-addon-torrent:latest .
```

#### Ejecutar el contenedor

Al ejecutar el contenedor, pasa la ruta al archivo de configuración de los buscadores de torrents utilizando una variable de entorno. Supongamos que el archivo de configuración está en `./config/torrent_searchers.json`.

```bash
docker run -d -p 8080:8080 \
  -e CONFIG_PATH=/path/del/archivo/config \
  -v /path/del/directorio/config:/config \
  --name stremio-addon-torrent \
  stremio-addon-torrent:latest
```

- `-e CONFIG_PATH`: Indica al contenedor la ruta donde está el directorio de configuración.
- `-v /path/del/archivo/config:/config`: Monta el directorio de configuración en el contenedor.

### Verificar la instalación

Una vez que el contenedor esté en ejecución, puedes acceder al addon en `http://localhost:7010`.

### Uso

- Accede a la URL del addon desde Stremio o cualquier navegador.
- Utiliza la interfaz para buscar contenido en los proveedores de torrents configurados.

## Desarrollo sin Docker

Si prefieres ejecutar la aplicación sin Docker:

1. Compila el proyecto con Maven:

   ```bash
   mvn clean install
   ```

2. Ejecuta la aplicación:

   ```bash
   java -jar target/stremio-addon-torrent.jar --torrent.searcher.config.path=/ruta/al/directorio/config
   ```

## Problemas comunes

1. **Archivo de configuración no encontrado:** Verifica la ruta del archivo de configuración y asegúrate de que el contenedor tiene acceso a esa ubicación.
2. **El contenedor no responde en `localhost:7010`:** Asegúrate de que el puerto 7010 esté disponible y que el contenedor esté en ejecución.

## Licencia

Este proyecto está bajo la licencia MIT. Para más detalles, consulta el archivo `LICENSE`.

---

¡Disfruta de la búsqueda de torrents en Stremio con tu propio addon personalizado!
