package fr.esupportail.esupstage.services;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.esupportail.esupstage.domain.jpa.entities.Contact;
import fr.esupportail.esupstage.domain.jpa.repositories.ContactRepository;
import fr.esupportail.esupstage.exception.NotFoundException;
import fr.esupportail.esupstage.services.beans.ContactBean;

@Service
public class ContactService {

	private final ContactRepository contactRepository;

	@Autowired
	public ContactService(final ContactRepository contactRepository) {
		super();
		this.contactRepository = contactRepository;
	}

	public Page<ContactBean> findAll(final Pageable pageable) {
		return this.contactRepository.findAll(pageable).map(ContactService::convert);
	}

	public ContactBean findById(final Integer id) {
		return this.contactRepository.findById(id).map(ContactService::convert)
				.orElseThrow(NotFoundException::new);
	}

	public ContactBean save(final ContactBean bean) {
		return ContactService.convert(this.contactRepository.save(ContactService.convert(bean)));
	}

	public void deleteById(final Integer id) {
		this.contactRepository.deleteById(id);
	}

	public static ContactBean convert(final Contact feed) {
		final ContactBean result = new ContactBean();
		BeanUtils.copyProperties(feed, result);
		return result;
	}

	public static Contact convert(final ContactBean feed) {
		final Contact result = new Contact();
		BeanUtils.copyProperties(feed, result);
		return result;
	}

}
