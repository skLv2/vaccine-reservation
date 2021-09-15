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
    @Autowired ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCompleted_UpdateStatus(@Payload ReservationCompleted reservationCompleted){

        if(!reservationCompleted.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + reservationCompleted.toJson() + "\n\n");



        // Sample Logic //
        Reservation tmp = reservationRepository.findById(Long.valueOf(reservationCompleted.getReservationid())).get();
        tmp.setStatus("Reservation Completed");
        reservationRepository.save(tmp);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelCompleted_UpdateStatus(@Payload CancelCompleted cancelCompleted){

        if(!cancelCompleted.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + cancelCompleted.toJson() + "\n\n");



        // Sample Logic //
        Reservation tmp = reservationRepository.findById(Long.valueOf(cancelCompleted.getReservationid())).get();
        tmp.setStatus("Reservation Canceled");
        reservationRepository.save(tmp);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
