<template>
  <div>
    <CFormGroup
            class="form-group form-row"
            v-if="'isAttribute' in item"
    >
      <template #label>
        <label :for="safeId()" class="col-form-label col-sm-3">
          {{ $t('common.attribute_label') }}
        </label>
      </template>
      <template #input>
        <CSwitch class="mx-1"
                 :id="safeId()"
                 color="primary"
                 :value.sync="item.isAttribute"
                 :checked="item.isAttribute === true"
                 variant="3d"
                 label-on="on"
                 label-off="off"
                 :disabled="item.className.includes('-')"
                 @update:checked="item.isAttribute = $event"
        />
      </template>
    </CFormGroup>
    <template v-for="(fieldOption, index) in fieldOptions">
      <CFormGroup
          class="form-group form-row"
          v-if="fieldOption.type === 'BOOLEAN'"
      >
        <template #label>
          <label :for="safeId()" class="col-form-label col-sm-3">
            <span :v-html="fieldOption.description"></span>
          </label>
        </template>
        <template #input>
          <CSwitch class="mx-1"
                   :id="safeId()"
                   color="primary"
                   :value.sync="fieldOption.value"
                   :checked="fieldOption.value === true"
                   variant="3d"
                   label-on="on"
                   label-off="off"
                   :disabled="item.className.includes('-')"
                   @update:checked="addToConfig(fieldOption, $event)" />
        </template>
      </CFormGroup>
      <CInput v-if="fieldOption.type === 'PASSWORD'"
              v-bind:key="fieldOption.key"
              v-bind:label="fieldOption.description"
              v-bind:placeholder="fieldOption.description"
              v-bind:value="displaySpecialCharacters(fieldOption.value)"
              @update:value="addToConfig(fieldOption, $event)"
              type="password"
              horizontal
              :disabled="item.className.includes('-')"
      />
      <!-- TOOLTIP -->
      <CInput v-if="(fieldOption.type === 'STRING' || fieldOption.type === 'OBJECT' || fieldOption.type === 'EXPRESSION') && fieldOption.possibleValues.length === 0"
              v-bind:key="fieldOption.key"
              v-bind:label="fieldOption.description"
              v-bind:placeholder="fieldOption.description"
              v-bind:value="displaySpecialCharacters(fieldOption.value)"
              @update:value="addToConfig(fieldOption, $event)"
              type="text"
              horizontal
              :disabled="item.className.includes('-')"
      >
        <template #prepend-content>
          <CIcon name="cil-code"
                 v-c-tooltip="{content: $t('common.support_expression_tooltip')}"
                 v-if="fieldOption.type === 'EXPRESSION'"/>
        </template>
      </CInput>
      <CSelect v-if="fieldOption.type === 'STRING' && fieldOption.possibleValues.length > 0"
               v-bind:label="fieldOption.description"
               :options="fieldOption.possibleValues"
               v-bind:value="fieldOption.value"
               @update:value="addToConfig(fieldOption, $event)"
               placeholder="Please select one"
               title="Select a value"
               horizontal
               :disabled="item.className.includes('-')"
      />
      <template v-if="fieldOption.type === 'REFERENCE'">
        <CSelect
            :options="datasets"
            label="Dataset"
            horizontal
            :placeholder="$t('common.select_one')"
            :title="$t('dataset.select_dataset_title')"
            @update:value="updateFields"
            required
        />
        <CSelect label="Field"
                 :options="fields"
                 :placeholder="$t('common.select_one')"
                 @update:value="addToConfig(fieldOption, $event)"
                 horizontal
                 required>
        </CSelect>
      </template>
      <CInput v-if="fieldOption.type === 'DATE'"
              v-bind:key="fieldOption.key"
              v-bind:label="fieldOption.description"
              v-bind:placeholder="fieldOption.description"
              v-bind:value="fieldOption.value"
              @update:value="addToConfig(fieldOption, $event)"
              type="date"
              horizontal
              :disabled="item.className.includes('-')"
      />
      <CInput v-if="fieldOption.type === 'TIME'"
              v-bind:key="fieldOption.key"
              v-bind:label="fieldOption.description"
              v-bind:placeholder="fieldOption.description"
              v-bind:value="fieldOption.value"
              @update:value="addToConfig(fieldOption, $event)"
              type="time"
              step="1"
              horizontal
              :disabled="item.className.includes('-')"
      />
      <CInput v-if="fieldOption.type === 'NUMERIC'"
              v-bind:key="fieldOption.key"
              v-bind:label="fieldOption.description"
              v-bind:placeholder="fieldOption.description"
              v-bind:value="fieldOption.value"
              @update:value="addToConfig(fieldOption, $event)"
              type="number"
              step="any"
              horizontal
              :disabled="item.className.includes('-')"
      />
      <template v-if="fieldOption.type === 'FIELD'">
        <CButton @click="redirectToNestedField" color="primary" class="mb-2" :disabled="item.className.includes('-')">{{ $t('dataset.set_field_button') }}</CButton>
      </template>
      <template v-if="fieldOption.type === 'FIELDS'">
        <CButton @click="redirectToNestedField" color="primary" class="mb-2" :disabled="item.className.includes('-')">{{ $t('dataset.set_fields_button') }}</CButton>
      </template>
      <div class="mb-3" v-if="fieldOption.type === 'LIST'">
        <!--
        <CSelect
          label="Choices"
          :options="item.config[fieldOption.key]"
          multiple="multiple"
          horizontal
        />
        -->
        <CFormGroup
            class="form-group form-row"
        >
          <template #label>
            <label :for="safeId()" class="col-form-label col-sm-3">
              {{ fieldOption.description }}
            </label>
          </template>
          <template #input>
            <div class="col-sm-9">
              <select name="choices" :id="'choices-' + fieldOption.key.replace(/\./g,'_')" multiple="multiple" class="custom-select selection form-control" size="10">
                <template v-for="(choice,index) in items[currentItem].config[fieldOption.key]">
                  <option
                          :key="index"
                          :data-key="index"
                          :value="choice">
                    {{choice}}
                  </option>
                </template>
              </select>
            </div>
          </template>
        </CFormGroup>
        <CInput type="text" :label="$t('dataset.choice_value_label')" :value.sync="fieldOption.choice" horizontal />
        <CButton @click="addToConfig(fieldOption, fieldOption.choice)" color="primary" class="mr-1">{{ $t('common.add_value_button') }}</CButton>
        <CButton @click="addToConfig(fieldOption, null)" color="primary" class="mr-1">{{ $t('common.clear_all_button') }}</CButton>
        <CButton @click="deleteChoices(fieldOption.key)" color="primary">{{ $t('common.delete_selected_button') }}</CButton>
      </div>
      <!--
      <CSelect v-if="fieldOption.type === 'LIST'"
              v-bind:key="fieldOption.key"
              v-bind:label="fieldOption.description"
              v-bind:placeholder="fieldOption.description"
              v-bind:value="fieldOption.value"
              @update:value="addToConfig(fieldOption, $event)"
              multiple="multiple"
              horizontal
              :disabled="item.className.includes('-')"
      />
      -->
    </template>
  </div>
</template>

<script>
  import makeUid from "@coreui/utils/src/make-uid";
  import { getAllPrimaryFieldOptions, getAllDatasetOptions } from "../../constants/api";

  export default {
    name: "FieldOptions",
    props: {
      fieldOptions: {
        type: Array,
        default: () => [
        ]
      },
      item: {
        type: Object
      },
      currentItem: {
        type: Number|String
      },
      items: {
        type: Array
      }
    },
    data() {
      return {
        datasets: [],
        fields: []
      }
    },
    mounted() {
      if (this.$route.query.workspaceId && this.datasets.length === 0) {
        getAllDatasetOptions(this.$route.query.workspaceId)
        .then(result => this.datasets = result)
        .catch(err => {})
      }
    },
    methods: {
      updateFields(datasetId) {
        getAllPrimaryFieldOptions(datasetId)
        .then(result => this.fields = result)
        .catch(err => {})
      },
      deleteChoices(configKey) {
        let choices = this.$el.querySelector('#choices-' + configKey.replace(/\./g,'_'))
        let values = this.items[this.currentItem].config[configKey]

        for (let i = 0; i < choices.selectedOptions.length; i++){
          values.splice(choices.selectedOptions[i].dataset.key, 1)
        }
        this.items.push()
        // console.log(choices.selectedOptions)
      },
      redirectToNestedField() {
        this.$emit('redirect-nested', this.item)
      },
      addToConfig(fieldOption, value) {
        let convertedValue = value
        // if (fieldOption.type === 'BOOLEAN') {
        //   convertedValue = value === 'true' || true
        if (fieldOption.type === 'NUMERIC') {
          convertedValue = parseFloat(value)
        }
        else if (fieldOption.type === 'STRING' || fieldOption.type === 'EXPRESSION') {
          convertedValue = this.escapeSpecialCharacters(value)
        } else if (fieldOption.type === 'LIST') {
          convertedValue = this.items[this.currentItem].config[fieldOption.key]

          if (value === null) {
            convertedValue.splice(0, convertedValue.length)
          } else {
            fieldOption.choice = ''
            let choices = this.$el.querySelector('#choices-' + fieldOption.key.replace(/\./g,'_'))
            choices.scrollTop = choices.scrollHeight;

            if (this.items[this.currentItem].config['field.choice.value.type'] === 'NUMERIC') {
              convertedValue.push(parseFloat(value))
            } else {
              convertedValue.push(value)
            }
          }
          //this.$set(this.items[this.currentItem].config, fieldOption.key, convertedValue)
          //this.items[this.currentItem].config[fieldOption.key] = convertedValue
          this.items.push()

          return
        }
        //this.$set(this.item.config, fieldOption.key, convertedValue)
        this.items[this.currentItem].config[fieldOption.key] = convertedValue
        this.items.push()
        this.$emit('config-change', fieldOption.key, convertedValue)
      },
      displaySpecialCharacters(value) {
        return value ? value.replace('\n', '\\n') : value === '' ? '' : null
      },
      escapeSpecialCharacters(value) {
        //return value
        return value ?  value.replace('\\n', '\n') : value === '' ? '' : null
      },
      safeId () {
        if (this.id || this.$attrs.id) {
          return this.id || this.$attrs.id
        }
        return makeUid()
      }
    }
  }
</script>

<style scoped>
  #choices{
    height: 150px;
    overflow-x: hidden;
    overflow-y: scroll;
    width: 150px;
  }
  #choices .selection{
    width:150px;
  }
</style>