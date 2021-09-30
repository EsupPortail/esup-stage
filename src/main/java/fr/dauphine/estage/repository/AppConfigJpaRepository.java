package fr.dauphine.estage.repository;

import fr.dauphine.estage.enums.AppConfigCodeEnum;
import fr.dauphine.estage.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AppConfigJpaRepository extends JpaRepository<AppConfig, String> {

    @Query("SELECT ac FROM AppConfig ac WHERE ac.code = :code")
    AppConfig findByCode(AppConfigCodeEnum code);
}
