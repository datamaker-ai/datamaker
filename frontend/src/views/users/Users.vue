<template>
  <CRow>
    <CCol col="12" xl="8">
      <transition name="slide">
        <CCard>
          <CCardHeader>
            Users
            <div class="float-right">
              <CButton to="users/create" color="primary" class="mr-1">{{ $t('users.create_user_button') }}</CButton>
              <CButton to="users/groups" color="primary">{{ $t('users.manage_groups_button') }}</CButton>
            </div>
          </CCardHeader>
          <CCardBody>
            <CDataTable
              hover
              striped
              sorter
              :items="items"
              :fields="fields"
              :items-per-page="perPage"
              pagination
              index-column
              :loading="isLoading"
            >
              <template #username="data">
                <td>
                  <strong>{{data.item.username}}</strong>
                </td>
              </template>
            
              <template #enabled="data">
                <td>
                  <CBadge :color="getBadge(data.item.enabled)">
                    {{data.item.enabled ? 'Enabled' : 'Disabled'}}
                  </CBadge>
                </td>
              </template>

              <template #edit_details="{item, index}">
                <td class="py-2" v-if="isAdmin()">
                  <CButton v-bind:to="'users/edit/' + item.externalId"
                           color="primary"
                           variant="outline"
                           square
                           size="sm"
                           class="mr-1"
                           :disabled="disabledButtons"
                  >
                    {{ $t('common.edit_button') }}
                  </CButton>
                  <CButton v-if="item.enabled && item.username !== 'admin'"
                           @click="changeStatus(item, false)"
                           color="primary"
                           variant="outline"
                           square
                           size="sm"
                           class="mr-1"
                           :disabled="disabledButtons"
                  >
                    {{ $t('common.disable_button') }}
                  </CButton>
                  <CButton v-if="!item.enabled && item.username !== 'admin'"
                           @click="changeStatus(item, true)"
                           color="primary"
                           variant="outline"
                           square
                           size="sm"
                           class="mr-1"
                           :disabled="disabledButtons"
                  >
                    {{ $t('common.enable_button') }}
                  </CButton>
                  <CButton v-if="item.username !== 'admin'"
                           @click="showDeleteWarning(item, index)"
                           color="primary"
                           variant="outline"
                           square
                           size="sm"
                           :disabled="disabledButtons"
                  >
                    {{ $t('common.delete_button') }}
                  </CButton>
                </td>
              </template>
            </CDataTable>
            <CModal
                :title="$t('common.delete_warning_title')"
                color="danger"
                :show.sync="dangerModal"
            >
              {{ $t('common.delete_warning_message', { item: currentItem ? currentItem.name : '' }) }}
              <template #footer>
                <CButton @click="dangerModal = false" color="danger">{{ $t('common.discard_action') }}</CButton>
                <CButton @click="deleteItem(currentItem, rowNumber)" color="success">{{ $t('common.accept_action') }}</CButton>
              </template>
            </CModal>
          </CCardBody>
          <!--
          <CCardFooter>
            <CButton to="users/create" color="primary" class="mr-1">Create user</CButton>
            <CButton to="groups" color="primary">Manage groups</CButton>
          </CCardFooter>
          -->
        </CCard>
      </transition>
    </CCol>
  </CRow>
</template>

<script>
import {api, processState} from "../../constants/api"
import {i18n} from "@/plugins/i18n";

export default {
  name: 'Users',
  data: () => {
    return {
      items: [],
      fields: [
        {
          key: 'username',
          label: i18n.t('common.name')
        },
        {
          key: 'dateCreated',
          label: i18n.t('users.date_created_label')
        },
        {
          key: 'authority',
          label: i18n.t('users.role_label')
        },
        {
          key: 'userType',
          label: 'Type'
        },
        {
          key: 'enabled',
          label: i18n.t('users.status_label')
        },
        {
          key: 'edit_details',
          label: '',
          _style: 'width:20%',
          sorter: false,
          filter: false
        }
      ],
      perPage: 5,
      disabledButtons: false,
      isLoading: false,
      dangerModal: false,
      currentItem: null,
      rowNumber: -1
    }
  },
  paginationProps: {
    align: 'center',
    doubleArrows: false,
    previousButtonHtml: 'prev',
    nextButtonHtml: 'next'
  },
  methods: {
    getBadge (status) {
      return status === true ? 'success'
        : status === false ? 'secondary'
          : status === 'Pending' ? 'warning'
            : status === 'Banned' ? 'danger' : 'primary'
    },
    userLink (id) {
      return `users/${id}`
    },
    rowClicked (item, index) {
      const userLink = this.userLink(item.externalId)
      this.$router.push({path: userLink})
    },
    isAdmin () {
      return sessionStorage.getItem("isAdmin") === "true"
    },
    changeStatus(item, status) {
      processState(this, "beforeSubmit")
      item.enabled = false
      api.put(`/user/${item.externalId}/status?enabled=${status}`)
      .then(response => {
        processState(this, "onSuccess")
        item.enabled = status
      }).catch(err => {
        processState(this, "onError", err)
      })
    },
    deleteItem(item, index) {
      processState(this, "beforeSubmit")

      api.delete(`/user/${item.externalId}`)
      .then(response => {
        processState(this, "onSuccess")
        this.items.splice(this.items.indexOf(item), 1)
      }).catch(err => {
        processState(this, "onError", err)
      })
    },
    showDeleteWarning(item, index) {
      this.dangerModal = true
      this.currentItem = item
      this.rowNumber = index
    },
  },
  mounted() {
    this.isLoading = true
    api.get('user')
    .then(response => {
      this.items = response.data.payload
      this.isLoading = false
    })
    .catch(err => {})
  },
}
</script>
