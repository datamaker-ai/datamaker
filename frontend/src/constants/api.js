import axios from 'axios';
import router from '../router'
import Vue from 'vue';

export const endpoint = process.env.VUE_APP_ENDPOINT_URL

export const api = axios.create({
  baseURL: process.env.VUE_APP_ENDPOINT_URL + '/api',
  crossdomain: true,
  withCredentials: true
});

export const metrics = axios.create({
  baseURL: process.env.VUE_APP_ENDPOINT_URL + '/actuator'
});

metrics.interceptors.response.use(response => {
  return response;
}, error => {
  if (error.response && (error.response.status === 401)) {
    sessionStorage.removeItem("isAdmin")
    sessionStorage.removeItem("isAuthenticated")

    router.push({path: '/login' })
    let response = { data: { payload: []}}
    return Promise.resolve(response)
  } else {
    //Vue.prototype.$eventHub.$emit('show-toast', error);
    return Promise.reject(error);
  }
});

api.interceptors.response.use(response => {
  return response;
}, error => {
  if (error.response && (error.response.status === 401)) {
    sessionStorage.removeItem("isAdmin")
    sessionStorage.removeItem("isAuthenticated")

    router.push({path: '/login' })
    let response = { data: { payload: []}}
    return Promise.resolve(response)
  } else {
    Vue.prototype.$eventHub.$emit('show-toast', error);
    return Promise.reject(error);
  }
});

function formatOptions(items) {
  let options = []
  for (let item of items) {
    options.push({
      label: item.name,
      value: item.externalId
    })
  }
  options.sort(function(a, b){
    if ( a.label < b.label ){
      return -1;
    }
    if ( a.label > b.label ){
      return 1;
    }
    return 0;
  });
  options.unshift({
    label: "Select",
    disabled: "disabled",
    value: ""
  })
  return options
}

export function getAllDatasetOptions(workspaceId) {
  return new Promise((resolve, reject) => {
    api.get(`/workspace/${workspaceId}/datasets`)
    .then(response => {
      return resolve(formatOptions(response.data.payload))
    })
    .catch(err => {
      return reject(err);
    })
  })
}

export function getAllPrimaryFieldOptions(datasetId) {
  return new Promise((resolve, reject) => {
    api.get(`/dataset/${datasetId}/fields?primaryOnly=true`)
    .then(response => {
      if (response.data.payload.length === 0) {
        return resolve([{
          label: "No primary field found in dataset (see documentation)",
          disabled: "disabled",
          value: ""
        }])
      }
      return resolve(formatOptions(response.data.payload))
    })
    .catch(err => {
      return reject(err);
    })
  })
}

export function getAllFieldOptions(datasetId) {
  return new Promise((resolve, reject) => {
    api.get(`/dataset/${datasetId}/fields`)
    .then(response => {
      return resolve(formatOptions(response.data.payload))
    })
    .catch(err => {
      return reject(err);
    })
  })
}

export function getWorkspaceOptions() {
  return new Promise((resolve, reject) => {
    api.get("/workspace")
    .then(response => {
      return resolve(formatOptions(response.data.payload))
    })
    .catch(err => {
      return reject(err);
    })
  })
}

let components = {}

export function refreshComponents() {
  components = {}
  getComponents().then()
}

export function getComponents() {

  if (Object.keys(components).length > 0) {
    return new Promise((resolve, reject) => {
      return resolve(components)
    })
  }

  return new Promise((resolve, reject) => {
        api.get('/messages/components')
        .then(response => {
          components = response.data.payload
          return resolve(components)
        })
        .catch(err => reject(err))
      }
  )
}

export function processState(obj, state, error) {
  switch(state) {
    case "beforeSubmit":
      obj.disabledButtons = true
      obj.displayError = false
      obj.isLoading = true
      break;
    case "onError":
      obj.displayError = true
      obj.disabledButtons = false
      obj.isLoading = false
      if ('dangerModal' in obj) {
        obj.dangerModal = false
      }
      if (error.response) {
        if (error.response.data) {
          obj.errorMessage = error.response.data.title
          obj.errorDetails = error.response.data.detail
        } else {
          obj.errorMessage = error.response
        }
      } else {
        obj.errorMessage = error
      }
      break;
    default:
      obj.displayError = false
      obj.disabledButtons = false
      obj.isLoading = false
      if ('dangerModal' in obj) {
        obj.dangerModal = false
      }
  }
}

export function isAdmin () {
  return sessionStorage.getItem("isAdmin") === 'true'
}

export function filterSinkByGenerator (generator) {
  return true
}