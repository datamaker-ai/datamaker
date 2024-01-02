import Vue from 'vue'
import Router from 'vue-router'
import {i18n} from "@/plugins/i18n";

// Containers
const TheContainer = () => import('@/containers/TheContainer')

// Viewsdashboard
const Dashboard = () => import('@/views/Dashboard')

// Views - Pages
const Page404 = () => import('@/views/pages/Page404')
const Page500 = () => import('@/views/pages/Page500')
const Login = () => import('@/views/pages/Login')
const Success = () => import('@/views/pages/Success')

const EditWorkspace = () => import('@/views/workspaces/EditWorkspace')
const CreateWorkspace = () => import('@/views/workspaces/CreateWorkspace')
const Workspaces = () => import('@/views/workspaces/Workspaces')

// Jobs
const EditGenerateDataJob = () => import('@/views/jobs/EditGenerateDataJob')
const JobExecutionLogs = () => import('@/views/jobs/JobExecutionLogs')
const GenerateDataJobs = () => import('@/views/jobs/GenerateDataJobs')
const GenerateDataJobsSummary = () => import('@/views/jobs/SummaryView')

// Datasets
const EditDataset = () => import('@/views/datasets/EditDataset')
const EditFields = () => import('@/views/datasets/EditFields')
const InferDataset = () => import('@/views/datasets/InferDataset')
const CreateDataset = () => import('@/views/datasets/CreateDataset')
const Datasets = () => import('@/views/datasets/Datasets')

// Field Mappings
const FieldMappings = () => import('@/views/mappings/FieldMappings')
const EditFieldMapping = () => import('@/views/mappings/EditFieldMapping')
const CreateFieldMapping = () => import('@/views/mappings/CreateFieldMapping')

// Users
const Users = () => import('@/views/users/Users')
const Groups = () => import('@/views/users/Groups')
const EditUser = () => import('@/views/users/EditUser')
const EditGroup = () => import('@/views/users/EditGroup')
const EditProfile = () => import('@/views/users/EditProfile')

// Health
const Info = () => import('@/views/system/Info')
const Logs = () => import('@/views/system/Logs')
const Health = () => import('@/views/system/Health')

// Sinks
const EditSinkConfiguration = () => import('@/views/sinks/EditSinkConfiguration')
const SinkConfigurations = () => import('@/views/sinks/SinkConfigurations')

// Files
const FileManager = () => import('@/views/system/FileManager')
const UploadFile = () => import('@/views/system/UploadFile')

// Search
const SearchResults = () => import('@/views/search/SearchResults')

Vue.use(Router)

let router = new Router({
  mode: 'history', // https://router.vuejs.org/api/#mode default=hash
  linkActiveClass: 'active',
  scrollBehavior: () => ({y: 0}),
  routes: configRoutes(),
  base: '/datamaker'
})

router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (sessionStorage.getItem('isAuthenticated') == null) {
      next({
        path: '/login',
        params: {nextUrl: to.fullPath}
      })
    } else {
      let isAdmin = sessionStorage.getItem('isAdmin')
      if (to.matched.some(record => record.meta.is_admin)) {
        if (isAdmin === "true") {
          next()
        } else {
          next({name: 'Dashboard'})
        }
      } else {
        next()
      }
    }
  } else if (to.matched.some(record => record.meta.guest)) {
    if (sessionStorage.getItem('isAuthenticated') == null) {
      next()
    } else {
      next({name: 'Dashboard'})
    }
  } else {
    next()
  }
})
export default router

function configRoutes() {
  return [
    {
      path: '/',
      redirect: '/dashboard',
      name:'pages.home',
      component: TheContainer,
      meta: {
        requiresAuth: true
      },
      children: [
        {
          path: 'dashboard',
          name: 'pages.dashboard',
          component: Dashboard,
          meta: {
            requiresAuth: true
          }
        },
        {
          path: 'search',
          name: 'pages.search_results',
          component: SearchResults,
          meta: {
            requiresAuth: true
          }
        },
        {
          path: 'mappings',
          meta: {
            label: 'pages.field_mappings',
            requiresAuth: true
          },
          component: {
            render(c) {
              return c('router-view')
            }
          },
          children: [
            {
              path: '',
              component: FieldMappings,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'create',
              name: 'pages.create_field_mappings',
              component: CreateFieldMapping,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'edit/:id',
              name: 'pages.edit_field_mappings',
              component: EditFieldMapping,
              props: (route) => ({ name: route.params.id }),
              meta: {
                requiresAuth: true
              }
            }
          ]
        },
        {
          path: 'sinks',
          meta: {
            requiresAuth: true,
            label: 'pages.sinks'
          },
          component: {
            render(c) {
              return c('router-view')
            }
          },
          children: [
            {
              path: '',
              component: SinkConfigurations,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'create',
              name: 'pages.create_sinks',
              component: EditSinkConfiguration,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'edit/:id',
              name: 'pages.edit_sinks',
              component: EditSinkConfiguration,
              props: (route) => ({ name: route.params.id }),
              meta: {
                requiresAuth: true
              }
            }
          ]
        },
        {
          path: 'profile',
          component: EditProfile,
          name: 'pages.edit_profile',
          meta: {
            requiresAuth: true
          }
        },
        {
          path: 'users',
          meta: {
            requiresAuth: true,
            label: 'pages.users',
          },
          component: {
            render(c) {
              return c('router-view')
            }
          },
          children: [
            {
              path: '',
              component: Users,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'groups',
              component: Groups,
              name: 'pages.groups',
              meta: {
                requiresAuth: true,
                is_admin : true
              }
            },
            {
              path: 'groups/create',
              component: EditGroup,
              name: 'pages.create_group',
              meta: {
                requiresAuth: true,
                is_admin : true
              }
            },
            {
              path: 'groups/edit/:id',
              component: EditGroup,
              name: 'pages.edit_group',
              meta: {
                requiresAuth: true,
                is_admin : true
              }
            },
            {
              path: 'create',
              component: EditUser,
              name: 'pages.create_user',
              meta: {
                requiresAuth: true,
                is_admin : true
              }
            },
            {
              path: 'edit/:id',
              component: EditUser,
              name: 'pages.edit_user',
              meta: {
                requiresAuth: true,
                is_admin : true
              }
            }
          ]
        },
        {
          path: 'system',
          redirect: '/system/info',
          name: 'pages.system',
          meta: {
            requiresAuth: true
          },
          component: {
            render(c) {
              return c('router-view')
            }
          },
          children: [
            {
              path: 'health',
              name: 'pages.health',
              component: Health,
              meta: {
                requiresAuth: true,
                is_admin : false
              }
            },
            {
              path: 'info',
              name: 'pages.info',
              component: Info,
              meta: {
                requiresAuth: true,
                is_admin : false
              }
            },
            {
              path: 'files',
              meta: {
                requiresAuth: true,
                is_admin : true
              },
              component: {
                render(c) {
                  return c('router-view')
                }
              },
              children: [
                {
                  path: '',
                  name: 'pages.files',
                  component: FileManager,
                  meta: {
                    requiresAuth: true,
                    is_admin : true
                  }
                },
                {
                  path: 'create',
                  name: 'pages.resource',
                  component: UploadFile,
                  meta: {
                    requiresAuth: true,
                    is_admin : true
                  }
                }
              ]
            },
            {
              path: 'logs',
              name: 'pages.logs',
              component: Logs,
              meta: {
                requiresAuth: true,
                is_admin : false
              }
            }
          ]
        },
        {
          path: 'workspaces',
          meta: {
            requiresAuth: true,
            label: 'pages.workspaces',
          },
          component: {
            render(c) {
              return c('router-view')
            }
          },
          children: [
            {
              path: '',
              component: Workspaces,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'create',
              name: 'pages.create_workspace',
              component: CreateWorkspace,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'edit/:id',
              component: EditWorkspace,
              meta: {
                requiresAuth: true
              }
            }
          ]
        },
        {
          path: 'jobs',
          meta: {
            requiresAuth: true,
            label: 'pages.jobs'
          },
          component: {
            render(c) {
              return c('router-view')
            }
          },
          children: [
            {
              path: '',
              component: GenerateDataJobsSummary,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'create',
              component: EditGenerateDataJob,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'list',
              component: GenerateDataJobs,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'edit/:id',
              component: EditGenerateDataJob,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: ':id/logs',
              name: 'pages.job_executions',
              component: JobExecutionLogs,
              meta: {
                requiresAuth: true
              }
            }
          ]
        },
        {
          path: 'datasets',
          meta: {
            requiresAuth: true,
            label: 'pages.datasets'
          },
          component: {
            render(c) {
              return c('router-view')
            }
          },
          children: [
            {
              path: '',
              component: Datasets,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'create',
              name: 'pages.create_dataset',
              component: CreateDataset,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'infer',
              name: 'pages.infer_dataset',
              component: InferDataset,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'edit/:id',
              name: 'pages.edit_dataset',
              component: EditDataset,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'fields/:id',
              name: 'pages.edit_fields',
              component: EditFields,
              meta: {
                requiresAuth: true
              }
            },
            {
              path: 'fields/:id/:fieldId',
              name: 'pages.nested_field',
              component: EditFields,
              meta: {
                requiresAuth: true
              }
            },
          ]
        }
      ]
    },
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: {
        guest: true
      }
    },
    {
      path: '/success',
      name: 'Success',
      component: Success,
      meta: {
        guest: true
      }
    },
    {
      path: '/404',
      name: 'Error404',
      component: Page404
    },
    {
      path: '/500',
      name: 'Error500',
      component: Page500
    },
    {
      path: '*',
      component: Page404
    }
  ]
}

