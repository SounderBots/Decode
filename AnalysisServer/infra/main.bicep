@description('The name of the workload/application. This will be used to generate resource names.')
param appName string = 'datalogger-analysis'

@description('The location for all resources.')
param location string = resourceGroup().location

@description('The name of the container image.')
param containerImageName string = 'analysisserver'

@description('The tag of the container image.')
param containerImageTag string = 'latest'

var uniqueSuffix = uniqueString(resourceGroup().id)
var acrName = '${replace(appName, '-', '')}${uniqueSuffix}'
var appServicePlanName = '${appName}-plan-${uniqueSuffix}'
var webAppName = '${appName}-${uniqueSuffix}'

// Azure Container Registry
resource acr 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: acrName
  location: location
  sku: {
    name: 'Basic'
  }
  properties: {
    adminUserEnabled: true // Enabling for simple GitHub Actions integration if needed, though Managed Identity is preferred for the App Service
  }
}

// App Service Plan
resource appServicePlan 'Microsoft.Web/serverfarms@2022-09-01' = {
  name: appServicePlanName
  location: location
  sku: {
    name: 'B1' // Basic tier, change as needed
    tier: 'Basic'
    size: 'B1'
    family: 'B'
    capacity: 1
  }
  kind: 'linux'
  properties: {
    reserved: true
  }
}

// Web App
resource webApp 'Microsoft.Web/sites@2022-09-01' = {
  name: webAppName
  location: location
  kind: 'app,linux,container'
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    serverFarmId: appServicePlan.id
    siteConfig: {
      linuxFxVersion: 'DOCKER|${acr.properties.loginServer}/${containerImageName}:${containerImageTag}'
      appSettings: [
        {
          name: 'WEBSITES_PORT'
          value: '8000'
        }
        {
          name: 'DOCKER_REGISTRY_SERVER_URL'
          value: 'https://${acr.properties.loginServer}'
        }
        {
          name: 'DOCKER_REGISTRY_SERVER_USERNAME'
          value: acr.listCredentials().username
        }
        {
          name: 'DOCKER_REGISTRY_SERVER_PASSWORD'
          value: acr.listCredentials().passwords[0].value
        }
      ]
    }
  }
}

output acrLoginServer string = acr.properties.loginServer
output acrName string = acr.name
output webAppName string = webApp.name
