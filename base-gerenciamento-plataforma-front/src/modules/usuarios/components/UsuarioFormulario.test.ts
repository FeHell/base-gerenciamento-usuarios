import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter, RouterView } from 'vue-router'
import UsuarioFormulario from './UsuarioFormulario.vue'

describe('saída do formulário de usuários', () => {
  it('preserva o rascunho ao continuar e navega apenas após o descarte', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/cadastros/usuarios',
          name: 'usuarios',
          component: { template: '<p>Pesquisa</p>' },
        },
        { path: '/cadastros/usuarios/novo', name: 'usuarios-novo', component: UsuarioFormulario },
      ],
    })
    await router.push({ name: 'usuarios-novo' })
    const tela = mount(RouterView, {
      global: {
        plugins: [createPinia(), router],
        stubs: { DialogoBase: { template: '<section role="dialog"><slot /></section>' } },
      },
    })
    await tela.get('#usuario-form-login').setValue('rascunho')
    const voltar = router.push({ name: 'usuarios' })
    await flushPromises()
    const dialogo = tela.get('[role="dialog"]')
    await dialogo.findAll('button')[0]?.trigger('click')
    await voltar
    expect(router.currentRoute.value.name).toBe('usuarios-novo')
    expect((tela.get('#usuario-form-login').element as HTMLInputElement).value).toBe('rascunho')

    const descartar = router.push({ name: 'usuarios' })
    await flushPromises()
    await tela.get('[role="dialog"]').findAll('button')[1]?.trigger('click')
    await descartar
    expect(router.currentRoute.value.name).toBe('usuarios')
    tela.unmount()
  })
})
