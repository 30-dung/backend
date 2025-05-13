package com.example.serversideclinet.repository;

import com.example.serversideclinet.dto.CityWithCountDTO;
import com.example.serversideclinet.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Integer> {
    @Query("SELECT s FROM Store s WHERE " +
            "(:cityProvince IS NULL OR s.cityProvince = :cityProvince) AND " +
            "(:district IS NULL OR s.district = :district)")
    List<Store> findByCityProvinceAndDistrict(@Param("cityProvince") String cityProvince,
                                              @Param("district") String district);

    @Query("SELECT new com.example.serversideclinet.dto.CityWithCountDTO(s.cityProvince, COUNT(s)) " +
            "FROM Store s WHERE s.cityProvince IS NOT NULL GROUP BY s.cityProvince")
    List<CityWithCountDTO> findCitiesWithStoreCount();

    @Query("SELECT new com.example.serversideclinet.dto.CityWithCountDTO(s.district, COUNT(s)) " +
            "FROM Store s WHERE s.district IS NOT NULL AND s.cityProvince = :cityProvince GROUP BY s.district")
    List<CityWithCountDTO> findDistrictsWithStoreCountByCity(@Param("cityProvince") String cityProvince);
}