import {i18n} from "@/plugins/i18n";

export default [
  {
    _name: 'CSidebarNav',
    _children: [
      {
        _name: 'CSidebarNavItem',
        name: "Dashboard",
        to: '/dashboard',
        icon: 'cil-speedometer',
        badge: {
          color: 'primary',
          text: 'NEW'
        }
      },
      {
        _name: 'CSidebarNavTitle',
        _children: ['Tools']
      },
      {
        _name: 'CSidebarNavItem',
        name: 'Datasets',
        to: '/datasets',
        icon: 'cil-list'
      },
      {
        _name: 'CSidebarNavItem',
        name: 'Field Mappings',
        to: '/mappings',
        icon: 'cil-copy'
      },
      {
        _name: 'CSidebarNavItem',
        name: 'Jobs',
        to: '/jobs',
        icon: 'cil-running'
      },
      {
        _name: 'CSidebarNavDropdown',
        name: 'System',
        route: '/system',
        icon: 'cil-chart-line',
        items: [
          {
            name: 'Health',
            to: '/system/health',
            icon: 'cil-chart-line'
          },
          {
            name: 'Info',
            to: '/system/info',
            icon: 'cil-bell'
          },
          {
            name: 'File Manager',
            to: '/system/files',
            admin: true,
            icon: 'cil-library-add'
          },
          {
            name: 'Logs',
            to: '/system/logs',
            icon: 'cil-file'
          }
        ]
      },
      {
        _name: 'CSidebarNavItem',
        name: 'Sinks',
        to: '/sinks',
        icon: 'cil-shower'
      },
      {
        _name: 'CSidebarNavItem',
        name: 'Workspaces',
        to: '/workspaces',
        icon: 'cil-folder-open'
      }
    ]
  }
]