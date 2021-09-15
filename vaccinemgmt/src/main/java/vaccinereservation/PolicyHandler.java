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
    @Autowired VaccineMgmtRepository vaccineMgmtRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverApprovalFinished_CheckReservation(@Payload ApprovalFinished approvalFinished){

        if(!approvalFinished.validate()) return;

        System.out.println("\n\n##### listener CheckReservation : " + approvalFinished.toJson() + "\n\n");



        // Sample Logic //
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
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCanceled_CancelReservation(@Payload ReservationCanceled reservationCanceled){

        if(!reservationCanceled.validate()) return;

        System.out.println("\n\n##### listener CancelReservation : " + reservationCanceled.toJson() + "\n\n");



        // Sample Logic //
        VaccineMgmt tmp = vaccineMgmtRepository.findByReservationid(Long.toString(reservationCanceled.getId()));
        tmp.setQty(0);
        tmp.setShelflife("NULL");
        tmp.setVaccinetype("NULL");
        tmp.setProductiondate("NULL");
        vaccineMgmtRepository.save(tmp);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
