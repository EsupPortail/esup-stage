package fr.esupportail.esupstage.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.esupportail.esupstage.domain.jpa.repositories.NiveauCentreRepository;
import fr.esupportail.esupstage.services.beans.NiveauCentreBean;
import fr.esupportail.esupstage.services.beans.NiveauCentreMapper;

@Service
public class NiveauCentreService {

	private final NiveauCentreRepository niveauCentreRepository;

	@Autowired
	public NiveauCentreService(final NiveauCentreRepository niveauCentreRepository) {
		this.niveauCentreRepository = niveauCentreRepository;
	}

	public Page<NiveauCentreBean> findAll(Pageable pageable) {
		return this.niveauCentreRepository.findAll(pageable).map(NiveauCentreMapper.INSTANCE::convert);
	}

	public List<NiveauCentreBean> findAll() {
		return this.findAll(Pageable.unpaged()).toList();
	}

}
