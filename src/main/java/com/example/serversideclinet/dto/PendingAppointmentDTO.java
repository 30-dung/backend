package com.example.serversideclinet.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class PendingAppointmentDTO {
    public PendingAppointmentDTO(Integer appointmentId, String time, String date, String serviceName, String customerFullName, String customerPhoneNumber, String customerEmail) {
        this.appointmentId = appointmentId;
        this.time = time;
        this.date = date;
        this.serviceName = serviceName;
        this.customerFullName = customerFullName;
        this.customerPhoneNumber = customerPhoneNumber;
        this.customerEmail = customerEmail;
    }

    private Integer appointmentId;
    private String time;
    private String date;
    private String serviceName;
    private String customerFullName;
    private String customerPhoneNumber;
    private String customerEmail;
}
