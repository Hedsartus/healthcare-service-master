package ru.netology.patient.service.medical;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MedicalServiceImplTest {

    @Test
    void medicalServiceImplTest() {
        // подготавливаем данные:
        HealthInfo healthInfo = new HealthInfo(new BigDecimal("36.6"), new BloodPressure(120, 80));

        List<PatientInfo> patients = new ArrayList<>();
        patients.add(new PatientInfo(
                "patient1",
                "Ivan",
                "Ivanov",
                LocalDate.of(1987, 7, 28),
                healthInfo));
        patients.add(new PatientInfo(
                "patient2",
                "Svetlana",
                "Samoylova",
                LocalDate.of(2000, 7, 9),
                healthInfo));
        patients.add(new PatientInfo(
                "patient3",
                "Vladivir",
                "Lenin",
                LocalDate.of(1924, 4, 22),
                healthInfo));

        PatientInfoRepository patientInfoRepository = mock(PatientInfoRepository.class);
        when(patientInfoRepository.getById("patient1")).thenReturn(patients.get(0));
        when(patientInfoRepository.getById("patient2")).thenReturn(patients.get(1));
        when(patientInfoRepository.getById("patient3")).thenReturn(patients.get(2));

        SendAlertService sendAlertService = mock(SendAlertService.class);
        doNothing().when(sendAlertService).send(any(String.class));

        List<String> expected = new ArrayList<>();
        expected.add("Warning, patient with id: patient1, need help");
        expected.add("Warning, patient with id: patient2, need help");

        // вызываем целевой метод:
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);

        medicalService.checkBloodPressure("patient1", new BloodPressure(120, 90));
        medicalService.checkTemperature("patient1", new BigDecimal("39"));

        medicalService.checkBloodPressure("patient2", new BloodPressure(180, 80));
        medicalService.checkTemperature("patient2", new BigDecimal("36.6"));

        medicalService.checkBloodPressure("patient3", new BloodPressure(120, 80));
        medicalService.checkTemperature("patient3", new BigDecimal("36.6"));

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendAlertService, times(2)).send(argumentCaptor.capture());
        List<String> actual = argumentCaptor.getAllValues();

        // производим проверку (сравниваем ожидание и результат):
        assertThat(expected, is(actual));
        assertNotEquals(patients.size(), actual.size());
        assertThrows(RuntimeException.class, () -> {
            medicalService.checkBloodPressure("qwerty", new BloodPressure(120, 80));
        });
    }
}