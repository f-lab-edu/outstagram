# outstagram
instagram을 모티브로 만든 SNS API 서버 프로젝트
<br>
<br>
<br>
## 📌 단순한 SNS API만 개발하지 않았습니다!
- 각 유저의 피드는 어떻게 구성하고 푸시 알림은 어떻게 구현하는지
    - 각 유저의 피드 목록은 Redis, Kafka를 활용한 비동기 방식으로 **push model** 구현(구현 중)
    - 푸시 알림도 Kafka를 활용한 비동기 처리(적용 예정)

- 동시에 여러 유저가 좋아요 눌렀을 때 발생할 수 있는 동시성 문제는 어떻게 해결하는지
    - 각종 DB Lock 조사 후, 상황에 맞는 Lock 적용(적용 완료)
- 분산 DB 환경에서 커서 기반 페이지네이션을 어떻게 구현하는지
    - ID로 정렬하기 위해서 Snowflake ID 구현(적용 예정)
- 객체 지향적으로 어떻게 설계하는지
    - 각종 추상화 및 AOP 도입

 ## 💥 프로젝트 중 고민한 이슈와 해결 방법
 1. [AOP를 통한 cross-cutting concern 걷어내기](https://velog.io/@nick9999/Outstagram-AOP%EB%A5%BC-%ED%86%B5%ED%95%B4-%ED%9A%A1%EB%8B%A8-%EA%B4%80%EC%8B%AC%EC%82%ACcross-cutting-concern-%EA%B1%B7%EC%96%B4%EB%82%B4%EA%B8%B0)
 
2. [동시성 문제 고민 및 해결방안](https://velog.io/@nick9999/Outstagram-%EC%A2%8B%EC%95%84%EC%9A%94-%EB%8F%99%EC%8B%9C%EC%84%B1-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0)
3. [이미지 처리1 - 추상화](https://velog.io/@nick9999/Outstagram-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EC%B2%98%EB%A6%AC-%EC%B6%94%EC%83%81%ED%99%94)
4. [간단한 DB 쿼리 리팩토링](https://velog.io/@nick9999/Outstagram-DB-%EC%BF%BC%EB%A6%AC-%EC%B5%9C%EC%A0%81%ED%99%94)
5. [무한 스크롤 도입 ~ Snowflake 도입까지 과정](https://velog.io/@nick9999/Outstagram-%EB%AC%B4%ED%95%9C-%EC%8A%A4%ED%81%AC%EB%A1%A4-%EA%B5%AC%ED%98%84%ED%95%98%EB%A0%A4%EB%8B%A4-Snowflake-ID-%EB%8F%84%EC%9E%85%ED%95%9C-%EC%9D%B4%EC%95%BC%EA%B8%B0)
6. [kafka를 활용한 피드 push model 구현 과정](https://velog.io/@nick9999/Outstagram-kafka%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-%ED%94%BC%EB%93%9C-push-model-%EA%B5%AC%ED%98%84-%EA%B3%BC%EC%A0%95)
7. [같은 클래스에서 @Cacheable 달린 메서드 호출하면 왜 안 적용될까...](https://velog.io/@nick9999/Outstagram-Cacheable%EC%9D%B4-%EB%9F%B0%ED%83%80%EC%9E%84-%EC%8B%9C%EC%97%90-%EB%AC%B4%EC%8B%9C%EB%90%98%EB%8A%94-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0)
8. [이미지 처리2 - 템플릿 메서드 패턴 적용](https://velog.io/@nick9999/Outstagram-%ED%85%9C%ED%94%8C%EB%A6%BF-%EB%A9%94%EC%84%9C%EB%93%9C-%ED%8C%A8%ED%84%B4%EC%9D%84-%EC%8B%A4%EC%A0%9C-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8%EC%97%90-%EC%A0%81%EC%9A%A9%ED%95%B4%EB%B3%B4%EA%B8%B0)
9. [lua script를 통한 Redis 동시성 이슈 해결](https://velog.io/@nick9999/Outstagram-Redis%EC%97%90%EC%84%9C%EB%8F%84-%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%9D%B4%EC%8A%88%EA%B0%80-%EB%B0%9C%EC%83%9D%ED%95%9C%EB%8B%A4%EA%B3%A0...-lua-script-%EC%A0%81%EC%9A%A9%EA%B8%B0)
10. [nGrinder를 활용해 부하 테스트 후 성능 튜닝](https://velog.io/@nick9999/Outstagram-nGrinder%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-%EB%B6%80%ED%95%98-%ED%85%8C%EC%8A%A4%ED%8A%B8-%ED%9B%84-%EC%84%B1%EB%8A%A5-%ED%8A%9C%EB%8B%9D)

    
## 🖥 프로토타입 
- 카카오 오븐을 활용해 간단한 프로토타입 제작
  ![프로토타입 전체](https://github.com/f-lab-edu/outstagram/assets/123347183/fa39dc16-aefc-4ca6-b375-6559b7f02b38)

## 🔨 기능 구현 및 API 시그니처 정의
- [프로토타입 & 기능 구현 & API 시그니처 정의](https://github.com/f-lab-edu/outstagram/wiki/%ED%94%84%EB%A1%9C%ED%86%A0%ED%83%80%EC%9E%85-&-%EA%B8%B0%EB%8A%A5-%EC%A0%95%EC%9D%98-&-API-%EC%8B%9C%EA%B7%B8%EB%8B%88%EC%B2%98-%EC%A0%95%EC%9D%98)

## ERD 설계
![drawSQL-image-export-2024-05-04](https://github.com/f-lab-edu/outstagram/assets/123347183/8dc4bdf9-0699-4933-83ab-03bf557853be)
