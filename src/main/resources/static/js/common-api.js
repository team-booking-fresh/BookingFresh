// localStorage에서 사용할 Access Token의 키
const TOKEN_KEY = 'accessToken';

// AT를 localStorage에 저장하는 함수
export function setAccessToken(token) {
    if (token) {
        localStorage.setItem(TOKEN_KEY, token);
    } else {
        localStorage.removeItem(TOKEN_KEY);
    }
}

// "인터셉터" 역할을 하는 공통 fetch 함수
export async function fetchWithAuth(url, options = {}) {

    if (!options.headers) {
        options.headers = {};
    }
    options.credentials = 'include'; // RT 쿠키를 주고받기 위해 필수

    // (인터셉트) localStorage에 AT가 있으면 헤더에 추가
    const accessToken = localStorage.getItem(TOKEN_KEY);
    if (accessToken) {
        options.headers['Authorization'] = `Bearer ${accessToken}`;
    }

    // Content-Type 기본값 설정 (필요시)
    if (options.body && !options.headers['Content-Type']) {
        options.headers['Content-Type'] = 'application/json';
    }

    // API 요청
    let response = await fetch(url, options);

    // AT 만료 이후 재발급 로직
    if (response.status === 401 || response.status === 403) {

        // AT 재발급 요청(/api/auth/refresh) 자체가 401인 경우
        if (url.includes('/api/auth/refresh')) {
            console.error("Refresh Token이 만료되었습니다. 로그아웃 처리합니다.");
            setAccessToken(null);
            // 에러를 발생시켜 catch 블록으로 넘김
            throw new Error('Session expired');
        }

        console.warn("Access Token 만료 감지. 토큰 재발급을 시도합니다.");

        // AT 재발급 시도 (RT 쿠키는 자동으로 전송됨)
        try {
            const refreshResponse = await fetch('/api/auth/refresh', {
                method: 'POST',
                credentials: 'include' // RT 쿠키 전송
            });

            if (refreshResponse.ok) {
                // 재발급 성공
                const data = await refreshResponse.json();
                setAccessToken(data.accessToken);

                options.headers['Authorization'] = `Bearer ${data.accessToken}`;
                console.log("토큰 재발급 성공. 원래 요청을 재시도합니다.");
                response = await fetch(url, options);

            } else {
                console.error("Refresh Token이 만료되었습니다. 로그아웃 처리합니다.");
                setAccessToken(null);
                throw new Error('Session expired');
            }
        } catch (e) {
            console.error("토큰 재발급 중 네트워크 오류:", e);
            setAccessToken(null);
            throw new Error('Session expired');
        }
    }

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({
            message: response.statusText || `HTTP error ${response.status}`
        }));

        throw new Error(errorData.message || `API 요청 실패 (Status: ${response.status})`);
    }

    return response;
}

// 페이지 로드 시 로그인 상태 확인 함수 - RT 만료 이후라면 로그아웃
export async function checkLoginStatus() {
    try {
        const response = await fetchWithAuth('/api/auth/refresh', {
            method: 'POST',
        });

        const data = await response.json();
        setAccessToken(data.accessToken);
        return true;

    } catch (error) {
        console.log("로그인 상태 아님:", error.message);
        setAccessToken(null);
        return false;
    }
}

// 서버 로그아웃 요청 함수
export async function requestLogout() {
    try {
        //
        await fetchWithAuth('/api/auth/logout', {
            method: 'POST'
        });
    } catch (error) {
        console.error("서버 로그아웃 요청 중 오류:", error);
    } finally {
        setAccessToken(null);
        console.log("클라이언트 로그아웃 완료.");
        window.location.href = '/';
    }
}