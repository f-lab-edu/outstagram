# outstagram
instagram을 모티브로 만든 SNS API 서버 프로젝트
<br>
<br>
### 단순한 SNS API만 개발하지 않았습니다!
- 각 유저의 피드는 어떻게 구성하고 푸시 알림은 어떻게 구현하는지
    - 각 유저의 피드 목록은 Redis, Kafka를 활용한 비동기 방식으로 push model 구현(구현 중)
    - 푸시 알림도 Kafka를 활용한 비동기 처리(적용 예정)

- 동시에 여러 유저가 좋아요 눌렀을 때 발생할 수 있는 동시성 문제는 어떻게 해결하는지
    - 각종 DB Lock 조사 후, 상황에 맞는 Lock 적용(적용 완료)
- 분산 DB 환경에서 커서 기반 페이지네이션을 어떻게 구현하는지
    - ID로 정렬하기 위해서 Snowflake ID 구현(적용 예정)
- 객체 지향적으로 어떻게 설계하는지
    - 각종 추상화 및 AOP 도입

 ### 프로젝트 중 고민한 이슈와 해결 방법
 
