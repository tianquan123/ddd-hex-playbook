(() => {
  const filters = [...document.querySelectorAll('[data-filter]')];
  const resources = [...document.querySelectorAll('[data-tags]')];

  const applyFilter = (value) => {
    filters.forEach((button) => {
      button.setAttribute('aria-pressed', String(button.dataset.filter === value));
    });
    resources.forEach((resource) => {
      const tags = (resource.dataset.tags || '').split(/\s+/);
      resource.hidden = value !== 'all' && !tags.includes(value);
    });
  };

  filters.forEach((button) => {
    button.addEventListener('click', () => applyFilter(button.dataset.filter));
  });

  document.querySelectorAll('details[data-code]').forEach((details) => {
    details.addEventListener('toggle', () => {
      const summary = details.querySelector('summary');
      if (summary) summary.setAttribute('aria-expanded', String(details.open));
    });
  });

  if (filters.length) applyFilter('all');
})();
