/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.licensing.services;

import io.harness.ModuleType;
import io.harness.licensing.beans.modules.AccountLicenseDTO;
import io.harness.licensing.beans.modules.ModuleLicenseDTO;
import io.harness.licensing.entities.modules.ModuleLicense;

import java.util.List;

public interface LicenseCrudService {
  ModuleLicenseDTO getModuleLicense(String accountId, ModuleType moduleType);
  List<ModuleLicenseDTO> getModuleLicenses(String accountIdentifier, ModuleType moduleType);
  AccountLicenseDTO getAccountLicense(String accountIdentifier);
  ModuleLicenseDTO getModuleLicenseById(String identifier);
  ModuleLicenseDTO createModuleLicense(ModuleLicenseDTO moduleLicense);
  ModuleLicenseDTO updateModuleLicense(ModuleLicenseDTO moduleLicense);

  ModuleLicense getCurrentLicense(String accountId, ModuleType moduleType);
  ModuleLicense createModuleLicense(ModuleLicense moduleLicense);
  ModuleLicense updateModuleLicense(ModuleLicense moduleLicense);
}
