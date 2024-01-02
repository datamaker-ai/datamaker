<template>
  <CContainer class="d-flex align-items-center min-vh-100">
    <CRow class="w-100 justify-content-center">
      <CCol md="6">
        <div class="clearfix">
          <h1 class="float-left display-3 mr-4">500</h1>
          <h4 class="pt-3">{{ $t('500.title') }}</h4>
          <p class="text-muted">{{ $t('500.message') }}</p>
        </div>
      </CCol>
    </CRow>
  </CContainer>
</template>

<script>
export default {
  name: 'Page500'
}
</script>
