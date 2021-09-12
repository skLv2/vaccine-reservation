![logo](https://user-images.githubusercontent.com/90441340/132812163-26e595de-1236-4150-b699-0992756ff78f.jpg)

# 백신 예약

클라우드 네이티브 애플리케이션의 개발에 요구되는 체크포인트 확인
- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW

# Table of contents

- [백신예약](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [ConfigMap 설정](#ConfigMap-설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
    - [Self healing](#Liveness-Probe)

# 서비스 시나리오

백신 예약 서비스

기능적 요구사항
1. 고객이 날짜, 병원을 선택하고 백신 예약 승인 요청을 한다.
2. 요청이 승인되면 백신관리에 전달 된다.
3. 백신관리에서 백신유형, 백신유효기간, 제조일자, 수량을 확인하고 예약을 완료한다.
4. 예약이 완료되면 고객의 예약 상태를 완료로 업데이트 한다.
5. 고객이 예약을 취소 요청을 한다.
6. 백신관리에서 예약ID, 백신유형, 백신유효기간, 제조일자, 수량을 확인하고 예약을 취소한다.
7. 예약이 취소되면 고객의 예약상태를 취소로 업데이트 한다.
8. 고객은 날짜, 병원, 백신유형, 백신유효기간, 제조일자, 예약상태를 확인할 수 있다.

비기능적 요구사항
1. 트랜잭션
    1. 승인이 되지 않은 예약 요청은 백신 예약을 할 수 없다.  Sync 호출 
2. 장애격리
    1. 백신관리 기능이 수행되지 않더라도 예약 요청 승인, 취소 요청을 을 받을 수 있다. Async (event-driven), Eventual Consistency
    1. 예약 요청 승인이 과중되면 고객을 잠시동안 받지 않고 예약 요청을 잠시후에 하도록 유도한다  Circuit breaker, fallback
3. 성능
    1. 백신 예약에 대한 정보 및 예약 상태 등을 한 화면에서 확인 할 수 있다. CQRS

# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?

# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
![asis](https://user-images.githubusercontent.com/90441340/132832765-2ee6cd26-2841-43cd-b9ab-666664ee2de1.jpg)

## TO-BE 조직 (Vertically-Aligned)
![tobe](https://user-images.githubusercontent.com/90441340/132832772-01fe71e5-1545-4f20-a31e-99ab56558371.jpg)

## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과 : http://www.msaez.io/#/storming/T57jg9xOfZNjxno4WWSzRwX0nwG2/e1a691320c2b3a0cdab793ec7b6488dc

### 이벤트 도출
![event1](https://user-images.githubusercontent.com/90441340/132835649-0ae59e25-46a5-4241-ba14-e56f1da4502b.jpg)

### 부적격 이벤트 탈락
![event2](https://user-images.githubusercontent.com/90441340/132835320-18abe37d-b751-4858-a5d0-01d774fc9815.jpg)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
        - 예약 시 > CustomerAuthenticatied : 고객인증이 완료되어야 승인요청 이벤트가 발생하는 ACID 트랜잭션을 적용이 필요하므로 ReservationPlaced이벤트와 통합하여 처리

### 액터, 커맨드 부착 및 어그리게잇으로 묶기
![msaez1](https://user-images.githubusercontent.com/90441340/132937802-c4c2d493-bd1a-4a3a-8995-5f95314f05c0.jpg)

- Customer의 Reservation, Approval의 Approval관리, vaccine의 vaccine관리는 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌

### 바운디드 컨텍스트로 묶기
![msaez2](https://user-images.githubusercontent.com/90441340/132938090-317ce728-5447-470c-a4cd-05eb67e026ff.jpg)

### 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)
![event3](https://user-images.githubusercontent.com/90441340/132938176-528c04f2-7769-4a0e-b899-55328a3860af.jpg)

### 완성된 1차 모형!
[event4](https://user-images.githubusercontent.com/90441340/132938193-26503282-64f8-46b4-abf1-5d5c17672070.jpg)
 
 - View Model 추가

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증
![event5](https://user-images.githubusercontent.com/90441340/132938539-4de24525-7ec8-4b26-91ac-f99fe364a59c.jpg)

    - 고객이 병원, 날짜를 선택하여 예약한다. (ok)
    - 승인을 받는다. (ok)
    - 예약승인이 완료되면 예약 내역이 백신관리자에게 전달된다. (ok)
    - 백신관리자는 백신유형, 수량, 유효기간, 제조일자를 선택후 예약을 완료한다.(ok)
    - 고객은 중간중간 예약 현황을 조회한다. (View-green sticker 의 추가로 ok)

![event6](https://user-images.githubusercontent.com/90441340/132939739-07387a65-c451-4f55-be8f-b0404b2c1096.jpg)

    - 고객이 예약을 취소할 수 있다. (ok)
    - 예약이 취소되면 백신예약 상태가 변경되고 백신슈형, 유효기간, 제조일자가 초기화되고, 수량이 0으로 바뀐다.(ok)  

### 비기능 요구사항에 대한 검증
![event4](https://user-images.githubusercontent.com/90441340/132938193-26503282-64f8-46b4-abf1-5d5c17672070.jpg)

- 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 예약 승인 요청 시 승인처리:  승인이 완료되지 않은 예약은 절대 받지 않는다는 정책에 따라, ACID 트랜잭션 적용. 예약 승인 요청시 승인처리에 대해서는 Request-Response 방식 처리
        - 승인 완료 시 백신 관리, 예약 완료 및 예약 상태 변경 처리:  승인에서  마이크로서비스로 예약완료내역이 전달되는 과정에 있어서 vaccinemgmt 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
        - 나머지 모든 inter-microservice 트랜잭션: 예약상태, 백신상태 등 모든 이벤트에 대해 MyPage처리 등, 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.
	- 백신 관리 기능이 수행되지 않더라도 예약 승인은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
        - 승인시스템이 과중되면 사용자를 잠시동안 받지 않고 승인을 잠시후에 하도록 유도한다  Circuit breaker, fallback

## 헥사고날 아키텍처 다이어그램 도출
![Hex](https://user-images.githubusercontent.com/90441340/132939649-638d8f91-b7a7-41ba-b499-8fdb90e93bef.jpg)

    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐



## 구현:
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)
