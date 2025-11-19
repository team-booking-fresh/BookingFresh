import { requestLogout } from '/js/common-api.js';


// 페이지가 처음 로드될 때 메인 인증 로직 실행
document.addEventListener('DOMContentLoaded', async () => {

    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            // 버튼 비활성화 (중복 클릭 방지)
            logoutBtn.disabled = true;
            logoutBtn.textContent = '로그아웃 중...';

            await requestLogout();

        });
    }
});