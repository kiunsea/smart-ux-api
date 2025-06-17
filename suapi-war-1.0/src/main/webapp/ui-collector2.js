(function () {
  function isVisible(el) {
    const style = window.getComputedStyle(el);
    return (
      el.offsetParent !== null &&
      style.visibility !== 'hidden' &&
      style.display !== 'none'
    );
  }

  function hasInlineEventHandler(el) {
    const inlineEvents = ['onclick', 'onchange', 'oninput', 'onkeydown', 'onkeyup', 'onmousedown', 'onmouseup'];
    return inlineEvents.some(evt => typeof el[evt] === 'function');
  }

  function hasDataAction(el) {
    return el.dataset && (el.dataset.action || el.getAttribute('data-action'));
  }

  function extractElementInfo(el) {
    const events = [];
    const inlineEvents = ['onclick', 'onchange', 'oninput', 'onkeydown', 'onkeyup', 'onmousedown', 'onmouseup'];

    inlineEvents.forEach(evt => {
      if (typeof el[evt] === 'function') {
        events.push(evt);
      }
    });

    if (el.dataset?.action) {
      events.push(`data-action: ${el.dataset.action}`);
    }

    return {
      tag: el.tagName.toLowerCase(),
      id: el.id || null,
      class: Array.from(el.classList).join(' ') || null,
      name: el.name || null,
      type: el.type || null,
      role: el.getAttribute('role') || null,
      text: el.innerText?.trim() || null,
      value: el.value || null,
      placeholder: el.placeholder || null,
      eventListeners: events
    };
  }

  function collectEventBoundElements() {
    const all = document.querySelectorAll('*');
    const result = [];

    all.forEach(el => {
      if (!isVisible(el)) return;

      const hasEvent = hasInlineEventHandler(el) || hasDataAction(el);
      if (hasEvent) {
        result.push(extractElementInfo(el));
      }
    });

    return result;
  }

  function storeCollectedElements(uiElements) {
    window.uiSnapshot = uiElements;
    console.log('[UI Snapshot] Event-bound elements:', uiElements);
  }

  let domChangeTimer = null;

  function scheduleDomExtraction() {
    if (domChangeTimer) clearTimeout(domChangeTimer);

    domChangeTimer = setTimeout(() => {
      const elements = collectEventBoundElements();
      storeCollectedElements(elements);
    }, 2000); // 무조건 2초 대기
  }

  function observeDomChanges() {
    const observer = new MutationObserver(() => {
      scheduleDomExtraction();
    });

    observer.observe(document.body, {
      childList: true,
      attributes: true,
      subtree: true,
      attributeFilter: ['style', 'class', 'onclick', 'onchange', 'oninput']
    });

    console.log('[UI Snapshot] DOM observer initialized (event-bound only).');
  }

  function init() {
    scheduleDomExtraction(); // 최초 로드 시 2초 후 수집
    observeDomChanges();     // 이후 DOM 변화 감지 시마다 2초 후 수집
  }

  if (document.readyState === 'complete') {
    init();
  } else {
    window.addEventListener('load', init);
  }
})();
