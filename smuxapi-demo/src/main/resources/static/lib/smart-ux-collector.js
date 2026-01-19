(function () {
  const SERVER_ENDPOINT = './collect';  // <-- 상대 경로로 변경

  function isVisible(el) {
    const style = window.getComputedStyle(el);
    return (
      el.offsetParent !== null &&
      style.visibility !== 'hidden' &&
      style.display !== 'none' &&
      el.offsetWidth > 0 && // 요소의 너비가 0보다 커야 함
      el.offsetHeight > 0    // 요소의 높이가 0보다 커야 함
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
    //const events = [];
    const eventTypes = [];
    const inlineEvents = ['onclick', 'onchange', 'oninput', 'onkeydown', 'onkeyup', 'onmousedown', 'onmouseup'];

    /**
    inlineEvents.forEach(evt => {
      if (typeof el[evt] === 'function') {
        events.push(evt);
      }
    });

    if (el.dataset?.action) {
      events.push(`data-action: ${el.dataset.action}`);
    }
    */
   
    inlineEvents.forEach(evt => {
      if (typeof el[evt] === 'function') {
        eventTypes.push(evt.substring(2)); // 'onclick' -> 'click'
      }
    });

    if (hasDataAction(el)) { // data-action 속성이 있으면 'data-action'을 이벤트 타입으로 추가
      eventTypes.push(`data-action:${el.dataset.action || el.getAttribute('data-action')}`);
    }

    // label 값 설정 (우선순위: innerText, value, placeholder, id)
    let label = el.innerText?.trim();
    if (!label && el.value) { // innerText가 없으면 value 확인
        label = el.value.trim();
    }
    if (!label && el.placeholder) { // value도 없으면 placeholder 확인
        label = el.placeholder.trim();
    }
    if (!label && el.id) { // placeholder도 없으면 id 확인
        label = el.id;
    }
    if (!label && el.tagName) { // 아무것도 없으면 태그 이름으로
        label = el.tagName.toLowerCase();
    }
    
    /**
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
    */
   
    return {
      id: el.id || null, // element id
      type: eventTypes.length > 0 ? eventTypes.join(',') : el.tagName.toLowerCase(), // 이벤트가 있으면 이벤트, 없으면 태그
      label: label, // 요청하신 "label" 속성
      selector: generateCssSelector(el),
      xpath: generateXPath(el),
      properties: {
        enabled: !el.hasAttribute('disabled') && !el.hasAttribute('readonly'), // 비활성화 속성 확인
        visible: isVisible(el)
      }
    };
  }
  
  // 주어진 ID 또는 태그 이름을 기반으로 간단한 XPath 생성
  function generateXPath(el) {
    if (el.id) {
      return `//*[@id='${el.id}']`;
    }
    // 부모를 거슬러 올라가거나 더 복잡한 XPath는 이 예제 범위를 넘어섭니다.
    // 여기서는 가장 기본적인 태그 XPath만 반환합니다.
    return `//${el.tagName.toLowerCase()}`;
  }
  
  // 주어진 ID 또는 클래스, 태그를 기반으로 CSS Selector 생성
  function generateCssSelector(el) {
    if (el.id) {
      return `#${el.id}`;
    }
    // 클래스가 있다면 첫 번째 클래스를 기반으로 생성 (더 복잡한 로직은 필요시 추가)
    if (el.classList.length > 0) {
      return `.${Array.from(el.classList).join('.')}`;
    }
    return el.tagName.toLowerCase(); // 태그 이름만 반환
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
        elements: uiElements,
        ai_model : ai_model
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
