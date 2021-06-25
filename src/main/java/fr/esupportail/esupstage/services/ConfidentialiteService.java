package fr.esupportail.esupstage.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.esupportail.esupstage.domain.jpa.repositories.ConfidentialiteRepository;
import fr.esupportail.esupstage.services.beans.ConfidentialiteBean;
import fr.esupportail.esupstage.services.beans.ConfidentialiteMapper;

@Service
public class ConfidentialiteService {

	private final ConfidentialiteRepository confidentialiteRepository;

	@Autowired
	public ConfidentialiteService(final ConfidentialiteRepository confidentialiteRepository) {
		super();
		this.confidentialiteRepository = confidentialiteRepository;
	}

	public Page<ConfidentialiteBean> findAll(Pageable pageable) {
		return confidentialiteRepository.findAll(pageable).map(ConfidentialiteMapper.INSTANCE::convert);
	}

	public List<ConfidentialiteBean> findAll() {
		return this.findAll(Pageable.unpaged()).toList();
	}

}
