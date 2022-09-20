import hljs from 'highlight.js/lib/core';

import kotlin from 'highlight.js/lib/languages/kotlin';

hljs.registerLanguage('kotlin', kotlin);

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('pre code:not(.language-mermaid)').forEach((block) => {
    hljs.highlightElement(block);
  });
});
