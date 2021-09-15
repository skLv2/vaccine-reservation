package vaccinereservation;

import vaccinereservation.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{

    @Autowired
    private MyPageRepository mypagerepository;

    //고객이 백신 예약을 요청함
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationPlaced_then_CREATE(@Payload ReservationPlaced reservationplaced) {
        try {

            if (!reservationplaced.validate()) return;
            
            System.out.println("\n\n##### listener ReservationPlaced : " + reservationplaced.toJson() + "\n\n");

            // view 객체 생성
            MyPage mypage = new MyPage();

            // view 객체에 이벤트의 Value 를 set 함
            mypage.setreservationid(Long.toString(reservationplaced.getId()));
            mypage.setCustomerid(reservationplaced.getCustomerid());
            mypage.setDate(reservationplaced.getDate());
            mypage.setHospitalid(reservationplaced.getHospitalid());
            mypage.setStatus(reservationplaced.getStatus());
            mypage.setVaccinetype("Wait Please!");

            // view 레파지 토리에 insert
            mypagerepository.save(mypage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //예약이 완료됨
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCompleted_then_UPDATE(@Payload ReservationCompleted reservationCompleted) {
        try {
            if (!reservationCompleted.validate()) return;

            System.out.println("\n\n##### listener ReservationCompleted : " + reservationCompleted.toJson() + "\n\n");

            // view 객체 조회
            MyPage mypage = mypagerepository.findByReservationid(reservationCompleted.getReservationid());

            System.out.println("\n\n##### listener  findByReservationid: " + mypage.getreservationid() + "\n\n");

            if(mypage != null){
                mypage.setVaccinetype(reservationCompleted.getVaccinetype());
                mypage.setStatus("Reservation Completed");
                // view 레파지 토리에 update
                mypagerepository.save(mypage);
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //예약이 취소됨
    @StreamListener(KafkaProcessor.INPUT)
    public void whenCancelCompleted_then_UPDATE(@Payload CancelCompleted cancelCompleted) {
        try {
            if (!cancelCompleted.validate()) return;

            System.out.println("\n\n##### listener CancelCompleted : " + cancelCompleted.toJson() + "\n\n");
            
            // view 객체 조회
            MyPage mypage = mypagerepository.findByReservationid(cancelCompleted.getReservationid());
            if(mypage != null){
                mypage.setVaccinetype(cancelCompleted.getVaccinetype());
                mypage.setStatus("Reservation Canceled");
                // view 레파지 토리에 update
                mypagerepository.save(mypage);
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

 
}
