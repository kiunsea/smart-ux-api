// actions를 실행하는 함수
export async function doActions(actions) {
    if (actions.length) {
        for (let i = 0; i < actions.length; i++) {
            const action = actions[i];
            try {
                if (action.type === 'click') {
                    const elem = document.querySelector('#'+action.id);
                    if (elem) {
                        elem.click();
                        await delay(200); // 클릭 후 약간 대기
                    }
                } else if (action.type === 'scroll') {
                    window.scrollTo(0, action.position || 0);
                    await delay(300); // 스크롤 후 대기
                } else if (action.type === 'setAttribute') {
                    const elem = document.querySelector('#'+action.id);
                    if (elem) {
                        elem.setAttribute(action.attrName, action.attrValue);
                    } else {
                        console.warn(`element not found for selector: ${action.id}`);
                    }
                } else if (action.type === 'navigate') {
                    // 남은 명령어를 저장
                    const remaining = actions.slice(i + 1);
                    localStorage.setItem('pendingActions', JSON.stringify(remaining));
                    // 페이지 이동
                    window.location.href = action.url;
                    return; // 이후 코드는 실행되지 않음
                }
            } catch (error) {
                console.error('action execution error:', error);
            }
        }
    }
  }
  
  // 대기용 delay 함수
  function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
  
  // 페이지 로드 시 pendingActions 처리
  window.addEventListener('DOMContentLoaded', async () => {
    const pending = localStorage.getItem('pendingActions');
    if (pending) {
      localStorage.removeItem('pendingActions'); // 한번만 실행하고 지움
      const actions = JSON.parse(pending);
      await doActions(actions);
    }
  });