export const appSetup = {
  production: true,
  serviceEndpoint: '/jme-jeap-nivel-quadrel-project-template/'
};

export const authConfig = {
  configPathSegment: 'ui-api/configuration/auth',
  clientId: 'jme-jeap-nivel-quadrel-project-template',
  systemName: 'jme',
  renewUserInfoAfterTokenRenew: true,
  silentRenew: true,
  silentRenewUrl: `${window.location.origin}/jme-jeap-nivel-quadrel-project-template/assets/auth/silent-renew.html`,
  useAutoLogin: false
};

export const appEnvironment = {
  production: appSetup.production,
  BACKEND_SERVICE_API: appSetup.serviceEndpoint,
  CONFIGURATION_PATH: 'ui-api/configuration/auth'
};
