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
```
   cd reservation
   mvn spring-boot:run
   
   cd approval
   mvn spring-boot:run
   
   cd vaccinemgmt
   mvn spring-boot:run
   
   cd mypage
   mvn spring-boot:run
   
   cd gateway
   mvn spring-boot:run
   
```


## CQRS

백신 예약/취소/매핑 등 총 Status 및 백신 종류 에 대하여 고객이 조회 할 수 있도록 CQRS 로 구현하였다.
- reservation, approval, vaccinemgmt 개별 Aggregate Status 를 통합 조회하여 성능 Issue 를 사전에 예방할 수 있다.
- 비동기식으로 처리되어 발행된 이벤트 기반 Kafka 를 통해 수신/처리 되어 별도 Table 에 관리한다
- Table 모델링
 <img width="546" alt="스크린샷 2021-09-12 오후 8 11 26" src="https://user-images.githubusercontent.com/29780972/132992563-95aa9578-c953-4cbf-9b44-6397779b3466.png">
 
 - mypage MSA PolicyHandler를 통해 구현
   ("ReservationPlaced" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)
   
   ![image](https://user-images.githubusercontent.com/29780972/132992616-f5e4bec9-45f8-41d1-9690-b09de491d224.png)
   
   
   ("ReservationCompleted" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)
   ![image](https://user-images.githubusercontent.com/29780972/132992637-3eac9a68-b14e-4b79-9c79-e95e00f76645.png)
   
   
   ("CancelCompleted" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)
   ![image](https://user-images.githubusercontent.com/29780972/132993526-6f462911-3825-4271-84f9-9ca62235116b.png)

   

- 실제로 view 페이지를 조회해 보면 모든 room에 대한 정보, 예약 상태, 결제 상태 등의 정보를 종합적으로 알 수 있다.
  
  ![image](https://user-images.githubusercontent.com/29780972/132992733-dcfb3280-4f6a-4e6c-9b9b-2082067cd941.png)



## API 게이트웨이

 1. gateway 스프링부트 App을 추가 후 application.yaml내에 각 마이크로 서비스의 routes 를 추가하고 gateway 서버의 포트를 8080 으로 설정함
          - application.yaml 예시

       ![image](https://user-images.githubusercontent.com/29780972/132992794-270910ab-22a4-46c1-bdd3-d100c8e50a8e.png)
       


## Correlation
vaccinereservation 프로젝트에서는 PolicyHandler에서 처리 시 어떤 건에 대한 처리인지를 구별하기 위한 Correlation-key 구현을 
이벤트 클래스 안의 변수로 전달받아 서비스간 연관된 처리를 정확하게 구현하고 있습니다. 

아래의 구현 예제를 보면

예약(Reservation)을 하면 동시에 연관된 백신관리(vaccineMgmt), 승인(approval) 등의 서비스의 상태가 적당하게 변경이 되고,
예약건의 취소를 수행하면 다시 연관된  백신관리(vaccineMgmt), 승인(approval) 등의 서비스의 상태값 등의 데이터가 적당한 상태로 변경되는 것을
확인할 수 있습니다.

- 백신 예약 요청
http POST http://localhost:8088/reservations customerid=OHM hospitalid=123 date=20210910
![image](https://user-images.githubusercontent.com/29780972/133015614-0f9e7fa9-5640-4781-a7c7-76c822b27862.png)

"status": "RSV_REQUESTED" 확인


- 예약 후 - 승인 상태
http GET http://localhost:8088/approvals  

![image](https://user-images.githubusercontent.com/29780972/133015696-5040a152-d1d8-4802-8fed-845eebed088d.png)

"status": "APV_COMPLETED" 확인

- 예약 및 승인 완료 후 - 백신 관리 상태
http GET http://localhost:8088/vaccineMgmts      
![image](https://user-images.githubusercontent.com/29780972/133015768-bde17c64-2505-471e-ae37-9d7e1355f48e.png)

reservationID 에 맞춰 백신종류, 수량, 유통기한 등 매핑 확인

- 예약 및 승인 완료 후 백신 관리까지 끝난 후 - 예약 상태
http GET http://localhost:8088/reservations
![image](https://user-images.githubusercontent.com/29780972/133015883-05b0af2e-7cad-49c9-a28f-67260e739225.png)


"status": "Reservation Completed" 확인
 -> 정상적으로 백신 예약이 완료 된 경우 최종 상태가 Reservaiton Completed

- 예약 취소
http PATCH http://localhost:8088/reservations/1 status=CANCEL_REQUESTED
![image](https://user-images.githubusercontent.com/29780972/133016556-cef0467c-a654-4587-aca8-96cd0988069f.png)

"status": "CANCEL_REQUESTED" 확인

- 취소 후 - 백신 상태
http GET http://localhost:8088/vaccineMgmts    
![image](https://user-images.githubusercontent.com/29780972/133016651-286a33bb-4f4d-4621-bb30-47e9147bf032.png)

취소 요청한 ID에 따라 수량 0으로 변함 및 백신 종류 등 NULL로 설정 변함 확인

- 취소 후 - 예약 상태
http GET http://localhost:8088/reservations       
![image](https://user-images.githubusercontent.com/29780972/133016914-acdd2c05-ec7d-4d1d-ac7d-e4ec6ab3a356.png)

"status": "Reservation Canceled" 




## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. (예시는 Reservation 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 현실에서 발생가는한 이벤트에 의하여 마이크로 서비스들이 상호 작용하기 좋은 모델링으로 구현을 하였다.

```
@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String customerid;
    private String hospitalid;
    private String date;
    private String status;

    @PostPersist
    public void onPostPersist(){

        System.out.println(" ============== 백신 예약 요청 ============== ");
        ReservationPlaced reservationPlaced = new ReservationPlaced();
        BeanUtils.copyProperties(this, reservationPlaced);
        reservationPlaced.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        vaccinereservation.external.Approval approval = new vaccinereservation.external.Approval();
        // mappings goes here
        /* 승인(approval) 동기 호출 진행 */
        /* 승인 진행 가능 여부 확인 후 백신매핑 */
        if(this.getStatus().equals("RSV_REQUESTED")){

            approval.setReservationid(Long.toString(this.getId()));
            approval.setStatus("APV_REQUESTED");
        }

        ReservationApplication.applicationContext.getBean(vaccinereservation.external.ApprovalService.class)
            .requestapproval(approval);

    }
    
    @PrePersist
    public void onPrePersist(){
        System.out.println(" ============== 백신 예약 요청 전 ============== ");
        status = "RSV_REQUESTED";
    }

    @PostUpdate
    public void onPostUpdate(){

        System.out.println(" ============== 백신 취소 요청 ============== ");

        if(this.getStatus().equals("CANCEL_REQUESTED") ){
            ReservationCanceled reservationCanceled = new ReservationCanceled();
            BeanUtils.copyProperties(this, reservationCanceled);
            reservationCanceled.publishAfterCommit();
        }

        

    }
    @PreRemove
    public void onPreRemove(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getCustomerid() {
        return customerid;
    }

    public void setCustomerid(String customerid) {
        this.customerid = customerid;
    }
    public String getHospitalid() {
        return hospitalid;
    }

    public void setHospitalid(String hospitalid) {
        this.hospitalid = hospitalid;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
```

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

```
package vaccinereservation;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="reservations", path="reservations")
public interface ReservationRepository extends PagingAndSortingRepository<Reservation, Long>{

}
```

- 적용 후 REST API 의 테스트

```
#reservation 서비스의 백신 예약 요청
http POST http://localhost:8088/reservations customerid=OHM hospitalid=123 date=20210910

#reservation 서비스의 백신 취소 요청
http PATCH http://localhost:8088/reservations/1 status=CANCEL_REQUESTED

#reservation 서비스의 백신 예약 상태 및 백신 종류 확인
http GET http://localhost:8088/reservations

#vaccineMgmts 서비스의 및 유통기한등 백신 정보 확인
http GET http://localhost:8088/vaccineMgmts   
```


## 동기식 호출(Sync) 과 Fallback 처리

분석단계에서의 조건 중 하나로 예약(reservation)->승인(approval) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient로 이용하여 호출하도록 한다.

- 승인 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
#ReservationService.java

@FeignClient(name="approval", url="${prop.aprv.url}")
public interface ApprovalService {
    @RequestMapping(method= RequestMethod.POST, path="/approvals")
    public void requestapproval(@RequestBody Approval approval);

}

```

- 예약 요청을 받은 직후(@PostPersist) 승인여부를 동기(Sync)로 요청하도록 처리

```
#Reservation.java

@PostPersist
    public void onPostPersist(){

        System.out.println(" ============== 백신 예약 요청 ============== ");
        ReservationPlaced reservationPlaced = new ReservationPlaced();
        BeanUtils.copyProperties(this, reservationPlaced);
        reservationPlaced.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        vaccinereservation.external.Approval approval = new vaccinereservation.external.Approval();
        // mappings goes here
        /* 승인(approval) 동기 호출 진행 */
        /* 승인 진행 가능 여부 확인 후 백신매핑 */
        if(this.getStatus().equals("RSV_REQUESTED")){

            approval.setReservationid(Long.toString(this.getId()));
            approval.setStatus("APV_REQUESTED");
        }

        ReservationApplication.applicationContext.getBean(vaccinereservation.external.ApprovalService.class)
            .requestapproval(approval);

    }
    
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인

```
# 승인 (approval) 서비스를 잠시 내려놓음 (ctrl+c)
```
```
# 예약 요청  - Fail

http POST http://localhost:8088/reservations customerid=OHM hospitalid=123 date=20210910
```

![image](https://user-images.githubusercontent.com/29780972/133050695-e81902a6-838c-4373-a628-9eea14bc9753.png)

```
# 결제서비스 재기동
cd approvals
mvn spring-boot:run
```

```
# 예약 요청  - Success

http POST http://localhost:8088/reservations customerid=OHM hospitalid=123 date=20210910

```

![image](https://user-images.githubusercontent.com/29780972/133050824-fd9f857b-e22b-45bd-948f-f4dcf4223133.png)


- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커 처리는 운영단계에서 설명한다.)


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

승인가 이루어진 후에 예약 시스템의 상태가 업데이트 되고, 백신관리 시스템의 상태 업데이트가 비동기식으로 호출된다.
- 이를 위하여 승인이 완료되면 승인 완료 되었다는 이벤트를 카프카로 송출한다. (Publish)

```
#Approval.java

@Entity
@Table(name="Approval_table")
public class Approval {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String reservationid;
    private String status;

    @PostPersist
    public void onPostPersist(){

        System.out.println(" ============== 예약 승인 요청 ============== ");

        ApprovalFinished approvalFinished = new ApprovalFinished();
        BeanUtils.copyProperties(this, approvalFinished);
        approvalFinished.publishAfterCommit();

    }
    
    ....

```


- 백신관리 시스템에서는 승인 완료된 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
@Service
public class PolicyHandler{
    @Autowired VaccineMgmtRepository vaccineMgmtRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverApprovalFinished_CheckReservation(@Payload ApprovalFinished approvalFinished){

        if(!approvalFinished.validate()) return;

        System.out.println("\n\n##### listener CheckReservation : " + approvalFinished.toJson() + "\n\n");
	
        // VaccineMgmt vaccineMgmt = new VaccineMgmt();
        // vaccineMgmtRepository.save(vaccineMgmt);
        VaccineMgmt vaccinemgmt = new VaccineMgmt();
        vaccinemgmt.setReservationid(approvalFinished.getReservationid());
        vaccinemgmt.setVaccinetype("모더나");
        vaccinemgmt.setProductiondate("2021-09-01");
        vaccinemgmt.setShelflife("2021-09-30");
        vaccinemgmt.setQty(1);
        vaccineMgmtRepository.save(vaccinemgmt);

    }
    
    ....

```

그 외 예약 승인/거부는 백신 관리와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 유지보수로 인해 잠시 내려간 상태 라도 예약을 받는데 문제가 없다.

```
# 백신관리 서비스 (vaccineMgmt) 를 잠시 내려놓음 (ctrl+c)
```

```
# 예약 요청  - Success
http POST http://localhost:8088/reservations customerid=OHM hospitalid=123 date=20210910
```

![image](https://user-images.githubusercontent.com/29780972/133051128-ec32ffd9-4c8e-4be7-a5a9-93638d3af1a8.png)

```
# 예약 상태 확인  - vaccineMgmt 서비스와 상관없이 예약 상태는 정상 확인
http GET http://localhost:8088/reservations
```

![image](https://user-images.githubusercontent.com/29780972/133051194-269034b4-2d0c-4d11-8b88-638a851b8390.png)

http://localhost:8081/reservations/3은 id=2와 달리 "status": "RSV_REQUESTED" 에서 끝난것을 확인


## 폴리글랏 퍼시스턴스

viewPage 는 H2가 아닌 RDB 계열의 데이터베이스인 Maria DB 를 사용하기로 하였다. 
기존의 Entity Pattern 과 Repository Pattern 적용과 데이터베이스 관련 설정 (pom.xml, application.yml) 을 변경하였으며, mypage pom.xml에 maria DB 의존성을 추가 하였다.
위 작업을 통해 maria DB를 부착하였으며 아래와 같이 작업 진행됨을 확인할 수 있다.

```
#MyPage.java

@Entity
@Table(name="MyPage_table")
public class MyPage {

}

#MyPageRepository.java

package vaccinereservation;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyPageRepository extends CrudRepository<MyPage, Long> {

    MyPage findByReservationid(String reservationid);

}

#pom.xml

    		<dependency> 
			<groupId>org.mariadb.jdbc</groupId> 
			<artifactId>mariadb-java-client</artifactId> 
		</dependency>

# application.yml
 jpa:
      show_sql: true
      #format_sql: true
      generate-ddl: true
      hibernate:
        ddl-auto: create-drop
  
  datasource:
    url: jdbc:mariadb://localhost:3306/VaccineReservation
    driver-class-name: org.mariadb.jdbc.Driver
    username:  ####   (계정정보 숨김처리)
    password:  ####   (계정정보 숨김처리)
    			
```

실제 MariaDB 접속하여 확인 시, 데이터 확인 가능 (ex. Reservation에서 객실 예약 요청한 경우)

![image](https://user-images.githubusercontent.com/29780972/132993463-ab6d81b5-0d03-4a44-b922-7cb7e5cf023f.png)



# 운영

## CI/CD 설정
각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하 buildspec.yml 에 포함되었다.

AWS CodeBuild 적용 현황
![1](https://user-images.githubusercontent.com/88864503/133553458-2ecf1f10-3c01-4b3d-bcaa-84f268f7848a.JPG)

webhook을 통한 CI 확인
![2](https://user-images.githubusercontent.com/88864503/133553865-81afb01b-dbec-4167-bac9-c8fd62ea5718.JPG)

AWS ECR 적용 현황
![3](https://user-images.githubusercontent.com/88864503/133553933-30d2ba69-ec96-4b26-838b-33e2d061bb70.JPG)

EKS에 배포된 내용
![4](https://user-images.githubusercontent.com/88864503/133554057-b6c08a0a-04ce-4dd5-bc47-f01e9776373d.JPG)

POST 결과
![image (3)](https://user-images.githubusercontent.com/90441340/133559842-6a03776f-5e1b-465e-8bea-eabf1384b45c.png)

GET 결과
![image](https://user-images.githubusercontent.com/90441340/133559721-ba7d924b-76eb-4a97-b4b5-2d3c97e1771a.png)
![image (1)](https://user-images.githubusercontent.com/90441340/133559794-7fa37299-5368-42f2-8775-7b9e1dbc65b8.png)
![image (2)](https://user-images.githubusercontent.com/90441340/133559797-f0942cec-0275-4c60-b0b7-7412a3a7d088.png)


## ConfigMap 설정


 동기 호출 URL을 ConfigMap에 등록하여 사용


 kubectl apply -f configmap

```
 apiVersion: v1
 kind: ConfigMap
 metadata:
   name: vaccine-configmap
   namespace: vaccines
 data:
   apiurl: "http://user02-gateway:8080"

```
buildspec 수정

```
              spec:
                containers:
                  - name: $_PROJECT_NAME
                    image: $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$_PROJECT_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION
                    ports:
                      - containerPort: 8080
                    env:
                    - name: apiurl
                      valueFrom:
                        configMapKeyRef:
                          name: vaccine-configmap
                          key: apiurl 
                        
```            
application.yml 수정
```
prop:
  aprv:
    url: ${apiurl}
``` 

동기 호출 URL 실행
![5](https://user-images.githubusercontent.com/88864503/133554760-b8d8b524-ebbf-46dc-ba32-1820cffcc023.JPG)

## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
siege -c100 -t10S -v --content-type "application/json" 'http://af9a234af8e354f5299f1d049a1b21c0-1150269307.ap-northeast-1.elb.amazonaws.com:8080/reservations

```

```
# buildspec.yaml 의 readiness probe 의 설정:

                    readinessProbe:
                      httpGet:
                        path: /actuator/health
                        port: 8080
                      initialDelaySeconds: 10
                      timeoutSeconds: 2
                      periodSeconds: 5
                      failureThreshold: 10
```

Customer 서비스 신규 버전으로 배포
![9](https://user-images.githubusercontent.com/88864503/133559167-4a2ede3c-ad33-43b6-b101-8759d56dd0c4.png)


배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.

## Liveness Probe

테스트를 위해 buildspec.yml을 아래와 같이 수정 후 배포

```
livenessProbe:
                      # httpGet:
                      #   path: /actuator/health
                      #   port: 8080
                      exec:
                        command:
                        - cat
                        - /tmp/healthy
```


![6](https://user-images.githubusercontent.com/88864503/133556583-3315fae7-de8a-4882-ad1d-8493fbd2daa8.png)

pod 상태 확인
 
 kubectl describe ~ 로 pod에 들어가서 아래 메시지 확인
 ```
 Warning  Unhealthy  26s (x2 over 31s)     kubelet            Liveness probe failed: cat: /tmp/healthy: No such file or directory
 ```

/tmp/healthy 파일 생성
```
kubectl exec -it pod/reservation-5576944554-q8jwf -n vaccines -- touch /tmp/healthy
```
![7](https://user-images.githubusercontent.com/88864503/133556724-7693dec2-41dd-430c-a3d3-389cc309bfca.png)

성공 확인
