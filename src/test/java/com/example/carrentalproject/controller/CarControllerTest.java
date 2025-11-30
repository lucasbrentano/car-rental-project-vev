package com.example.carrentalproject.controller;

import com.example.carrentalproject.constant.FuelType;
import com.example.carrentalproject.constant.GearBoxType;
import com.example.carrentalproject.domain.Car;
import com.example.carrentalproject.domain.CarPackage;
import com.example.carrentalproject.domain.CarParameters;
import com.example.carrentalproject.dto.CarDto;
import com.example.carrentalproject.dto.CarPackageDto;
import com.example.carrentalproject.service.CarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CarControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CarService carService;

    @InjectMocks
    private CarController carController;

    private List<Car> mockCars;
    private Car mockCar;
    private CarDto mockCarDto;
    private List<CarPackage> mockCarPackages;
    private CarPackage mockCarPackage;
    private CarPackageDto mockCarPackageDto;
    private CarParameters mockCarParameters;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(carController).build();
        objectMapper = new ObjectMapper();

        // Setup Car data
        mockCar = Car.builder()
                .id(1L)
                .registrationNr("ABC1234")
                .brand("Audi")
                .model("A4")
                .isAvailable(true)
                .build();

        Car car2 = Car.builder()
                .id(2L)
                .registrationNr("XYZ5678")
                .brand("BMW")
                .model("M5")
                .isAvailable(true)
                .build();

        Car car3 = Car.builder()
                .id(3L)
                .registrationNr("DEF9012")
                .brand("Mercedes")
                .model("C300")
                .isAvailable(true)
                .build();

        mockCars = Arrays.asList(mockCar, car2, car3);

        // Setup CarDto data
        mockCarDto = CarDto.builder()
                .registrationNr("ABC1234")
                .brand("Audi")
                .model("A4")
                .isAvailable(true)
                .build();

        // Setup CarPackage data
        mockCarPackage = CarPackage.builder()
                .id(1L)
                .packageName("Sporty")
                .pricePerHour(100)
                .build();

        CarPackage carPackage2 = CarPackage.builder()
                .id(2L)
                .packageName("Luxury")
                .pricePerHour(200)
                .build();

        mockCarPackages = Arrays.asList(mockCarPackage, carPackage2);

        // Setup CarPackageDto data
        mockCarPackageDto = CarPackageDto.builder()
                .packageName("Sporty")
                .pricePerHour(100)
                .build();

        // Setup CarParameters data
        mockCarParameters = CarParameters.builder()
                .id(1L)
                .fuelType(FuelType.DIESEL)
                .gearBoxType(GearBoxType.MANUAL)
                .build();
    }

    // GET /cars 

    @Test
    void itShouldReturnAllCarsWithPageNull() throws Exception {
        when(carService.getAllCars(null, Sort.Direction.ASC))
                .thenReturn(mockCars);

        mockMvc.perform(get("/cars")
                        .param("sort", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(carService).getAllCars(null, Sort.Direction.ASC);
    }

    @Test
    void itShouldReturnAllCarsWithPagePresent() throws Exception {
        when(carService.getAllCars(1, Sort.Direction.ASC))
                .thenReturn(mockCars);

        mockMvc.perform(get("/cars")
                        .param("page", "1")
                        .param("sort", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(carService).getAllCars(1, Sort.Direction.ASC);
    }

    @Test
    void itShouldReturnAllCarsWithSortASC() throws Exception {
        when(carService.getAllCars(null, Sort.Direction.ASC))
                .thenReturn(mockCars);

        mockMvc.perform(get("/cars")
                        .param("sort", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[2].id").value(3L));

        verify(carService).getAllCars(null, Sort.Direction.ASC);
    }

    @Test
    void itShouldReturnAllCarsWithSortDESC() throws Exception {
        List<Car> sortedDesc = Arrays.asList(
                Car.builder().id(3L).registrationNr("DEF9012").brand("Mercedes").model("C300").build(),
                Car.builder().id(2L).registrationNr("XYZ5678").brand("BMW").model("M5").build(),
                Car.builder().id(1L).registrationNr("ABC1234").brand("Audi").model("A4").build()
        );
        when(carService.getAllCars(null, Sort.Direction.DESC))
                .thenReturn(sortedDesc);

        mockMvc.perform(get("/cars")
                        .param("sort", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[2].id").value(1L));

        verify(carService).getAllCars(null, Sort.Direction.DESC);
    }

    @Test
    void itShouldReturnCarsWithCorrectReturnStructure() throws Exception {
        when(carService.getAllCars(null, Sort.Direction.ASC))
                .thenReturn(mockCars);

        mockMvc.perform(get("/cars")
                        .param("sort", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].registrationNr").isNotEmpty())
                .andExpect(jsonPath("$[0].brand").isNotEmpty())
                .andExpect(jsonPath("$[0].model").isNotEmpty())
                .andExpect(jsonPath("$[0].isAvailable").isNotEmpty());
    }

    // GET /cars/packages 

    @Test
    void itShouldReturnAllCarPackagesOnCall() throws Exception {
        when(carService.getCarPackages())
                .thenReturn(mockCarPackages);

        mockMvc.perform(get("/cars/packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(carService).getCarPackages();
    }

    @Test
    void itShouldReturnCarPackagesWithCorrectStructure() throws Exception {
        when(carService.getCarPackages())
                .thenReturn(mockCarPackages);

        mockMvc.perform(get("/cars/packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].packageName").value("Sporty"))
                .andExpect(jsonPath("$[0].pricePerHour").value(100))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].packageName").value("Luxury"))
                .andExpect(jsonPath("$[1].pricePerHour").value(200));

        verify(carService).getCarPackages();
    }

    // GET /cars/available 

    @Test
    void itShouldReturnAvailableCarsWithPageNull() throws Exception {
        List<Car> availableCars = mockCars;
        when(carService.getAvailableCars(null, Sort.Direction.ASC))
                .thenReturn(availableCars);

        mockMvc.perform(get("/cars/available")
                        .param("sort", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(carService).getAvailableCars(null, Sort.Direction.ASC);
    }

    @Test
    void itShouldReturnAvailableCarsWithPagePresent() throws Exception {
        when(carService.getAvailableCars(1, Sort.Direction.ASC))
                .thenReturn(mockCars);

        mockMvc.perform(get("/cars/available")
                        .param("page", "1")
                        .param("sort", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(carService).getAvailableCars(1, Sort.Direction.ASC);
    }

    @Test
    void itShouldReturnAvailableCarsWithSortASC() throws Exception {
        when(carService.getAvailableCars(null, Sort.Direction.ASC))
                .thenReturn(mockCars);

        mockMvc.perform(get("/cars/available")
                        .param("sort", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(carService).getAvailableCars(null, Sort.Direction.ASC);
    }

    @Test
    void itShouldReturnAvailableCarsWithSortDESC() throws Exception {
        List<Car> sortedDesc = Arrays.asList(
                Car.builder().id(3L).registrationNr("DEF9012").brand("Mercedes").model("C300").isAvailable(true).build(),
                Car.builder().id(2L).registrationNr("XYZ5678").brand("BMW").model("M5").isAvailable(true).build(),
                Car.builder().id(1L).registrationNr("ABC1234").brand("Audi").model("A4").isAvailable(true).build()
        );
        when(carService.getAvailableCars(null, Sort.Direction.DESC))
                .thenReturn(sortedDesc);

        mockMvc.perform(get("/cars/available")
                        .param("sort", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L));

        verify(carService).getAvailableCars(null, Sort.Direction.DESC);
    }

    @Test
    void itShouldReturnAvailableCarsWithCorrectReturnStructure() throws Exception {
        when(carService.getAvailableCars(null, Sort.Direction.ASC))
                .thenReturn(mockCars);

        mockMvc.perform(get("/cars/available")
                        .param("sort", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].brand").isNotEmpty())
                .andExpect(jsonPath("$[0].isAvailable").value(true));
    }

    // GET /cars/{id} 

    @Test
    void itShouldReturnCarWithCorrectId() throws Exception {
        when(carService.getCar(1L))
                .thenReturn(mockCar);

        mockMvc.perform(get("/cars/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.brand").value("Audi"));

        verify(carService).getCar(1L);
    }

    @Test
    void itShouldReturnCarWithCorrectReturnStructure() throws Exception {
        when(carService.getCar(1L))
                .thenReturn(mockCar);

        mockMvc.perform(get("/cars/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.registrationNr").value("ABC1234"))
                .andExpect(jsonPath("$.brand").value("Audi"))
                .andExpect(jsonPath("$.model").value("A4"))
                .andExpect(jsonPath("$.isAvailable").value(true));

        verify(carService).getCar(1L);
    }

    // POST /cars 

    @Test
    void itShouldSaveCarOnPostCall() throws Exception {
        when(carService.saveCar(any(CarDto.class)))
                .thenReturn(mockCar);

        mockMvc.perform(post("/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCarDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.brand").value("Audi"));

        verify(carService).saveCar(any(CarDto.class));
    }

    @Test
    void itShouldReturnSavedCarWithCorrectStructure() throws Exception {
        when(carService.saveCar(any(CarDto.class)))
                .thenReturn(mockCar);

        mockMvc.perform(post("/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCarDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.registrationNr").value("ABC1234"))
                .andExpect(jsonPath("$.brand").value("Audi"))
                .andExpect(jsonPath("$.model").value("A4"));

        verify(carService).saveCar(any(CarDto.class));
    }

    // PUT /cars/{id} 

    @Test
    void itShouldEditCarWithIdAndDto() throws Exception {
        when(carService.editCar(anyLong(), any(CarDto.class)))
                .thenReturn(mockCar);

        mockMvc.perform(put("/cars/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCarDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.brand").value("Audi"));

        verify(carService).editCar(eq(1L), any(CarDto.class));
    }

    @Test
    void itShouldReturnEditedCarWithCorrectStructure() throws Exception {
        CarDto updatedCarDto = CarDto.builder()
                .registrationNr("ABC1234")
                .brand("Audi")
                .model("A6")
                .isAvailable(false)
                .build();

        Car updatedCar = Car.builder()
                .id(1L)
                .registrationNr("ABC1234")
                .brand("Audi")
                .model("A6")
                .isAvailable(false)
                .build();

        when(carService.editCar(anyLong(), any(CarDto.class)))
                .thenReturn(updatedCar);

        mockMvc.perform(put("/cars/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCarDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.model").value("A6"))
                .andExpect(jsonPath("$.isAvailable").value(false));

        verify(carService).editCar(eq(1L), any(CarDto.class));
    }

    // PUT /cars/{id}/parameters 

    @Test
    void itShouldSetCarParametersWithIdAndParameters() throws Exception {
        Car carWithParameters = Car.builder()
                .id(1L)
                .registrationNr("ABC1234")
                .brand("Audi")
                .model("A4")
                .isAvailable(true)
                .carParameters(mockCarParameters)
                .build();

        when(carService.setCarParameters(anyLong(), any(CarParameters.class)))
                .thenReturn(carWithParameters);

        mockMvc.perform(put("/cars/1/parameters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCarParameters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.carParameters.fuelType").value("DIESEL"))
                .andExpect(jsonPath("$.carParameters.gearBoxType").value("MANUAL"));

        verify(carService).setCarParameters(eq(1L), any(CarParameters.class));
    }

    // PUT /cars/{id}/packages 

    @Test
    void itShouldSetCarPackageWithIdAndPackageName() throws Exception {
        Car carWithPackage = Car.builder()
                .id(1L)
                .registrationNr("ABC1234")
                .brand("Audi")
                .model("A4")
                .isAvailable(true)
                .carPackage(mockCarPackage)
                .build();

        when(carService.setCarPackage(anyLong(), eq("Sporty")))
                .thenReturn(carWithPackage);

        mockMvc.perform(put("/cars/1/packages")
                        .param("packageName", "Sporty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.carPackage.packageName").value("Sporty"))
                .andExpect(jsonPath("$.carPackage.pricePerHour").value(100));

        verify(carService).setCarPackage(eq(1L), eq("Sporty"));
    }

    // DELETE /cars/{id} 

    @Test
    void itShouldDeleteCarWithId() throws Exception {
        doNothing().when(carService).deleteCar(anyLong());

        mockMvc.perform(delete("/cars/1"))
                .andExpect(status().isOk());

        verify(carService).deleteCar(eq(1L));
    }

    // POST /cars/packages 

    @Test
    void itShouldSaveCarPackageOnPostCall() throws Exception {
        when(carService.saveCarPackage(any(CarPackageDto.class)))
                .thenReturn(mockCarPackage);

        mockMvc.perform(post("/cars/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCarPackageDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.packageName").value("Sporty"));

        verify(carService).saveCarPackage(any(CarPackageDto.class));
    }

    @Test
    void itShouldReturnSavedCarPackageWithCorrectStructure() throws Exception {
        when(carService.saveCarPackage(any(CarPackageDto.class)))
                .thenReturn(mockCarPackage);

        mockMvc.perform(post("/cars/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCarPackageDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.packageName").value("Sporty"))
                .andExpect(jsonPath("$.pricePerHour").value(100));

        verify(carService).saveCarPackage(any(CarPackageDto.class));
    }

    // DELETE /cars/packages/{id} 

    @Test
    void itShouldDeleteCarPackageWithId() throws Exception {
        doNothing().when(carService).deleteCarPackage(anyLong());

        mockMvc.perform(delete("/cars/packages/1"))
                .andExpect(status().isOk());

        verify(carService).deleteCarPackage(eq(1L));
    }
}
