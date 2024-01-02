export default [
  {
    _name: 'CSidebarNav',
    _children: [
      {
        _name: 'CSidebarNavItem',
        name: 'Tableau de bord',
        to: '/dashboard',
        icon: 'cil-speedometer',
        badge: {
          color: 'primary',
          text: 'NOUVEAU'
        }
      },
      {
        _name: 'CSidebarNavTitle',
        _children: ['Outils']
      },
      {
        _name: 'CSidebarNavItem',
        name: 'Jeux de données',
        to: '/datasets',
        icon: 'cil-list'
      },
      {
        _name: 'CSidebarNavItem',
        name: 'Mappage Champs',
        to: '/mappings',
        icon: 'cil-copy'
      },
      {
        _name: 'CSidebarNavItem',
        name: 'Tâches',
        to: '/jobs',
        icon: 'cil-running'
      },
      {
        _name: 'CSidebarNavDropdown',
        name: 'Système',
        route: '/system',
        icon: 'cil-chart-line',
        items: [
          {
            name: 'Statut',
            to: '/system/health',
            icon: 'cil-chart-line'
          },
          {
            name: 'Info',
            to: '/system/info',
            icon: 'cil-bell'
          },
          {
            name: 'Gestionnaire Fichiers',
            to: '/system/files',
            icon: 'cil-library-add',
            admin: true
          },
          {
            name: 'Journaux',
            to: '/system/logs',
            icon: 'cil-file'
          }
        ]
      },
      {
        _name: 'CSidebarNavItem',
        name: 'Collecteurs',
        to: '/sinks',
        icon: 'cil-shower'
      },
      {
        _name: 'CSidebarNavItem',
        name: 'Projets',
        to: '/workspaces',
        icon: 'cil-folder-open'
      }
    ]
  }
]