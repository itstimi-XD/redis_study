import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 100 }, // 1분 동안 100명까지 증가
    { duration: '3m', target: 100 }, // 3분 동안 100명 유지
    { duration: '1m', target: 0 },   // 1분 동안 0명까지 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'], // 95%의 요청이 200ms 이하
    http_req_failed: ['rate<0.01'],   // 실패율 1% 미만
  },
};

export default function () {
  const BASE_URL = 'http://localhost:8080';

  // 랜덤 검색어 생성 (10%의 확률로 검색어 사용)
  const useSearchParam = Math.random() < 0.1;
  let url = `${BASE_URL}/api/movies`;

  if (useSearchParam) {
    // 미리 정의된 영화 제목 샘플 중 하나 선택
    const movieTitles = ['아바타', '인터스텔라', '매트릭스', '터미네이터'];
    const randomTitle = movieTitles[Math.floor(Math.random() * movieTitles.length)];
    url += `?title=${randomTitle}`;
  }

  const res = http.get(url);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 200ms': (r) => r.timings.duration < 200,
  });

  sleep(Math.random() * 3); // 1~3초 대기 (사용자 행동 시뮬레이션)
}