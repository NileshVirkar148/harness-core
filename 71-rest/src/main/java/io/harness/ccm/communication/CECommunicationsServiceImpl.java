package io.harness.ccm.communication;

import com.google.inject.Inject;

import io.harness.ccm.communication.entities.CECommunications;
import io.harness.ccm.communication.entities.CommunicationType;

import java.util.List;

public class CECommunicationsServiceImpl implements CECommunicationsService {
  @Inject CECommunicationsDao ceCommunicationsDao;

  @Override
  public CECommunications get(String accountId, String email, CommunicationType type) {
    return ceCommunicationsDao.get(accountId, email, type);
  }

  @Override
  public List<CECommunications> list(String accountId, String email) {
    return ceCommunicationsDao.list(accountId, email);
  }

  @Override
  public void update(String accountId, String email, CommunicationType type, boolean enable, boolean selfEnabled) {
    CECommunications entry = get(accountId, email, type);
    if (entry == null) {
      entry = CECommunications.builder()
                  .accountId(accountId)
                  .emailId(email)
                  .type(type)
                  .enabled(enable)
                  .selfEnabled(selfEnabled)
                  .build();
      ceCommunicationsDao.save(entry);
    } else {
      ceCommunicationsDao.update(accountId, email, type, enable);
    }
  }

  @Override
  public void delete(String accountId, String email, CommunicationType type) {
    CECommunications entry = get(accountId, email, type);
    if (entry != null) {
      ceCommunicationsDao.delete(entry.getUuid());
    }
  }

  @Override
  public List<CECommunications> getEntriesEnabledViaEmail(String accountId) {
    return ceCommunicationsDao.getEntriesEnabledViaEmail(accountId);
  }

  public List<CECommunications> getEnabledEntries(String accountId, CommunicationType type) {
    return ceCommunicationsDao.getEnabledEntries(accountId, type);
  }
}
