package com.example.serversideclinet.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PendingAppointmentDTO {
    private Integer appointmentId;
    private String time;
    private String date;
    private String serviceName;
    private String customerFullName;
    private String customerPhoneNumber;
    private String customerEmail;
}
