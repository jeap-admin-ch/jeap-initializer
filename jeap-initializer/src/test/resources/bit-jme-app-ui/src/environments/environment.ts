export const appSetup = {
  production: false,
  serviceEndpoint: 'http://localhost:8080/jme-jeap-nivel-quadrel-project-template/'
};

export const authConfig = {
  configPathSegment: 'ui-api/configuration/auth',
  clientId: 'jme-jeap-nivel-quadrel-project-template',
  systemName: 'jme',
  renewUserInfoAfterTokenRenew: true,
  silentRenew: true,
  silentRenewUrl: `${window.location.origin}/assets/auth/silent-renew.html`,
  useAutoLogin: false
};

export const appEnvironment = {
  production: appSetup.production,
  BACKEND_SERVICE_API: appSetup.serviceEndpoint,
  CONFIGURATION_PATH: 'ui-api/configuration/auth'
};
