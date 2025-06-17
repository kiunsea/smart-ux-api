(function () {
  const SERVER_ENDPOINT = '/suapi/collect';  // <-- 실제 서버 URL로 교체하세요

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

  function storeAndSend(uiElements) {
    window.uiSnapshot = uiElements;
    console.log('[UI Snapshot] Event-bound elements:', uiElements);

    // 전송
    fetch(SERVER_ENDPOINT, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        timestamp: new Date().toISOString(),
        elements: uiElements
      })
    }).then(response => {
      if (!response.ok) {
        throw new Error(`Server responded with status ${response.status}`);
      }
      console.log('[UI Snapshot] Sent to server successfully.');
    }).catch(err => {
      console.error('[UI Snapshot] Failed to send to server:', err);
    });
  }

  let domChangeTimer = null;

  function scheduleDomExtraction() {
    if (domChangeTimer) clearTimeout(domChangeTimer);

    domChangeTimer = setTimeout(() => {
      const elements = collectEventBoundElements();
      storeAndSend(elements);
    }, 2000); // 2초 대기 후 수집 및 전송
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

    console.log('[UI Snapshot] DOM observer initialized.');
  }

  function observeScreen() {
    scheduleDomExtraction(); // 최초 로딩 시 2초 후 실행
    observeDomChanges();     // 이후 DOM 변경 시마다 2초 대기 후 실행
  }

  if (document.readyState === 'complete') {
    observeScreen();
  } else {
    window.addEventListener('load', observeScreen);
  }
})();
