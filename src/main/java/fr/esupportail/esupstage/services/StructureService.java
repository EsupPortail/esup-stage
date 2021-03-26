package fr.esupportail.esupstage.services;

import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.repositories.StructureRepository;
import fr.esupportail.esupstage.exception.NotFoundException;
import fr.esupportail.esupstage.services.beans.StructureBean;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StructureService {

	private final StructureRepository structureRepository;

	@Autowired
	public StructureService(final StructureRepository structureRepository) {
		super();
		this.structureRepository = structureRepository;
	}

	public Page<StructureBean> findAll(final Pageable pageable) {
		return this.structureRepository.findAll(pageable).map(StructureService::convert);
	}

	public StructureBean findById(final Integer id) {
		return this.structureRepository.findById(id).map(StructureService::convert)
				.orElseThrow(NotFoundException::new);
	}

	public StructureBean save(final StructureBean bean) {
		return StructureService.convert(this.structureRepository.save(StructureService.convert(bean)));
	}

	public void deleteById(final Integer id) {
		this.structureRepository.deleteById(id);
	}

	public static StructureBean convert(final Structure feed) {
		final StructureBean result = new StructureBean();
		BeanUtils.copyProperties(feed, result);
		return result;
	}

	public static Structure convert(final StructureBean feed) {
		final Structure result = new Structure();
		BeanUtils.copyProperties(feed, result);
		return result;
	}

}
