package fr.esupportail.esupstage.services;

import fr.esupportail.esupstage.domain.jpa.entities.Pays;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.repositories.PaysRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.StructureRepository;
import fr.esupportail.esupstage.exception.NotFoundException;
import fr.esupportail.esupstage.services.beans.PaysBean;
import fr.esupportail.esupstage.services.beans.StructureBean;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PaysService {

	private final PaysRepository paysRepository;

	@Autowired
	public PaysService(final PaysRepository paysRepository) {
		super();
		this.paysRepository = paysRepository;
	}

	public Page<PaysBean> findAll(final Pageable pageable) {
		return this.paysRepository.findAll(pageable).map(PaysService::convert);
	}

	public PaysBean findById(final Integer id) {
		return this.paysRepository.findById(id).map(PaysService::convert)
				.orElseThrow(NotFoundException::new);
	}

	public PaysBean save(final PaysBean bean) {
		return PaysService.convert(this.paysRepository.save(PaysService.convert(bean)));
	}

	public void deleteById(final Integer id) {
		this.paysRepository.deleteById(id);
	}

	public static PaysBean convert(final Pays feed) {
		final PaysBean result = new PaysBean();
		BeanUtils.copyProperties(feed, result);
		return result;
	}

	public static Pays convert(final PaysBean feed) {
		final Pays result = new Pays();
		BeanUtils.copyProperties(feed, result);
		return result;
	}

}
