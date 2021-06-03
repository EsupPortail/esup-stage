package fr.esupportail.esupstage.services;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.esupportail.esupstage.domain.jpa.entities.Service;
import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.domain.jpa.repositories.ServiceRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.StructureRepository;
import fr.esupportail.esupstage.exception.NotFoundException;
import fr.esupportail.esupstage.services.beans.ServiceBean;
import fr.esupportail.esupstage.services.beans.StructureBean;


@org.springframework.stereotype.Service
public class ServiceService {
	private final ServiceRepository serviceRepository;

	@Autowired
	public ServiceService(final ServiceRepository serviceRepository) {
		super();
		this.serviceRepository = serviceRepository;
	}

	public Page<ServiceBean> findAll(final Pageable pageable) {
		return this.serviceRepository.findAll(pageable).map(ServiceService::convert);
	}

	public ServiceBean findById(final Integer id) {
		return this.serviceRepository.findById(id).map(ServiceService::convert)
				.orElseThrow(NotFoundException::new);
	}

	public ServiceBean save(final ServiceBean bean) {
		return ServiceService.convert(this.serviceRepository.save(ServiceService.convert(bean)));
	}

	public void deleteById(final Integer id) {
		this.serviceRepository.deleteById(id);
	}

	public static ServiceBean convert(final Service feed) {
		final ServiceBean result = new ServiceBean();
		BeanUtils.copyProperties(feed, result);
		return result;
	}

	public static Service convert(final ServiceBean feed) {
		final Service result = new Service();
		BeanUtils.copyProperties(feed, result);
		return result;
	}

}
