<template>
  <CForm id="workspace">
    <CInput
        :description="$t('workspaces.name_description')"
        :label="$t('workspaces.name_label')"
        horizontal
        :placeholder="$t('workspaces.name_placeholder')"
        v-model="workspace.name"
        required
    />
    <CInput
        :description="$t('workspaces.description_description')"
        :label="$t('workspaces.description_label')"
        horizontal
        :placeholder="$t('workspaces.description_placeholder')"
        v-model="workspace.description"
    />
    <!-- TODO get current owner and support auto-complete -->
    <CInput
        :description="$t('workspaces.owner_description')"
        :label="$t('workspaces.owner_label')"
        horizontal
        :placeholder="$t('workspaces.owner_placeholder')"
        v-model="workspace.owner"
        required
    />
    <CSelect
        :value.sync="workspace.group"
        :label="$t('workspaces.group_label')"
        :options="getGroupOptions()"
        :placeholder="$t('workspaces.group_placeholder')"
        horizontal
    >
    </CSelect>
    <div class="form-group form-row">
      <CCol tag="label" sm="3" class="col-form-label">
        {{ $t('workspaces.group_permissions') }}
      </CCol>
      <CCol sm="9">
        <CInputRadio
            v-for="(option, optionIndex) in options"
            :label="option"
            :key="optionIndex"
            :value="option"
            @update:checked="workspace.groupPermissions = option"
            name="permissions"
            custom
            :checked="workspace.groupPermissions === option"
            required
        />
      </CCol>
    </div>
    <template v-if="loading">
      <CElementCover
          :boundaries="[
            { sides: ['top'], query: 'form' },
            { sides: ['bottom'], query: 'form' }
          ]"
      />
    </template>
  </CForm>
</template>

<script>
  import {api, processState} from "../../constants/api";

  export default {
    name: 'WorkspaceForm',
    props: {
      isLoading: {
        type: Boolean,
        default: false
      },
      workspace: {
        type: Object,
        // Object or array defaults must be returned from
        // a factory function
        default: function () {
          return {
            name: '',
            description: '',
            owner: '',
            group: '',
            groupPermissions: 'NONE'
          }
        }
      },
      options: {
        type: Array,
        default: () => ['NONE', 'READ_ONLY', 'READ_EXECUTE', 'READ_WRITE', 'FULL']
      }
    },
    data () {
      return {
        groups: [],
        loading: this.isLoading
      }
    },
    methods: {
      getGroupOptions() {
        let options = []

        for (let group of this.groups) {
          options.push({label: group.name, value: group.externalId })
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
        options.unshift({value: null, label: "Select one...", config: [], disabled: "disabled", selected: "selected"})
        return options
      },
    },
    mounted() {
      this.loading = true

      api.get('user/groups')
        .then(response => {
          this.groups = response.data.payload
          this.loading = false
        })
        .catch()
    }
  }
</script>
