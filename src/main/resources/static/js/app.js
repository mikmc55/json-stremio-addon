$(document).ready(function () {
  // Inicializar DataTables
  $('#searchersTable').DataTable();

  // Obtener y mostrar la lista de buscadores al cargar la página
  fetchSearchers();

  // Guardar o editar un buscador
  $('#searcherForm').submit(function (event) {
    event.preventDefault();

    const searcher = {
      id: $('#searcherId').val() || null,
      name: $('#searcherName').val(),
      type: $('#searcherType').val(),
      url: $('#searcherUrl').val(),
      description: $('#searcherDescription').val()
    };

    const endpoint = searcher.id ? `/searchers/save` : `/searchers/save`;

    $.ajax({
      url: endpoint,
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(searcher),
      success: function () {
        $('#searcherModal').modal('hide');
        fetchSearchers();
      },
      error: function () {
        alert('Failed to save searcher');
      }
    });
  });

  function fetchSearchers() {
    $.get('/searchers', function (data) {
      const table = $('#searchersTable').DataTable();
      table.clear();  // Limpiar los datos anteriores

      data.forEach((searcher) => {
        const row = [
          searcher.id,
          searcher.name,
          searcher.type,
          searcher.url,
          searcher.description,
          `
            <button class="btn btn-primary btn-sm edit-btn" data-id="${searcher.id}">Edit</button>
            <button class="btn btn-outline-danger btn-sm delete-btn" data-id="${searcher.id}">Delete</button>
          `
        ];
        table.row.add(row);
      });

      table.draw();  // Dibujar los datos en la tabla
    });
  }

  $(document).on('click', '.edit-btn', function () {
    const searcherId = $(this).data('id');
    $.get(`/searchers/${searcherId}`, function (searcher) {
      $('#searcherId').val(searcher.id);
      $('#searcherName').val(searcher.name);
      $('#searcherType').val(searcher.type);
      $('#searcherUrl').val(searcher.url);
      $('#searcherDescription').val(searcher.description);
      $('#searcherModal').modal('show');
    });
  });

  $(document).on('click', '.delete-btn', function () {
    const searcherId = $(this).data('id');
    if (confirm('Are you sure you want to delete this searcher?')) {
      $.ajax({
        url: `/searchers/delete/${searcherId}`,
        type: 'DELETE',
        success: function () {
          fetchSearchers();
        },
        error: function () {
          alert('Failed to delete searcher');
        }
      });
    }
  });

  $('#searcherModal').on('show.bs.modal', function () {
    $('#searcherForm')[0].reset();
    $('#searcherId').val('');
  });
});
