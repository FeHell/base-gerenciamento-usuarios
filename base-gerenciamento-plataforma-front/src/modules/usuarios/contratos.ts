export const rotulosStatus = {
  ATIVO: 'Ativo',
  BLOQUEADO: 'Bloqueado',
  PENDENTE: 'Pendente',
  INATIVO: 'Inativo',
} as const

export const rotulosTipo = {
  PLATAFORMA: 'Administrador da plataforma',
  APLICACAO: 'Administrador de aplicação',
  USUARIO: 'Usuário',
} as const

export type StatusUsuario = keyof typeof rotulosStatus
export type TipoAcesso = keyof typeof rotulosTipo

export interface Usuario {
  id: string
  login: string
  email: string
  status: StatusUsuario
  dataCriacao: string
  permissoesPlataforma: string[]
}

export interface UsuarioListagem extends Usuario {
  tipo: TipoAcesso
  permissoes: { aplicacao: string; codigo: string }[]
}

export interface Pagina<T> {
  itens: T[]
  pagina: number
  tamanho: number
  total: number
}

export interface FiltrosUsuarios {
  login: string
  email: string
  status: StatusUsuario | ''
  tipo: TipoAcesso | ''
}

export interface DashboardUsuarios {
  total: number
  recentes: number
  porTipo: { tipo: TipoAcesso; total: number }[]
  porPermissao: { aplicacao: string; codigo: string; total: number }[]
  porStatus: { status: StatusUsuario; total: number }[]
  porMes: { mes: string; total: number }[]
}

export interface NovoUsuario {
  login: string
  email: string
  senha: string
  status: StatusUsuario
  permissoesPlataforma: string[]
}

export type AtualizarUsuario = Pick<Usuario, 'login' | 'email' | 'status' | 'permissoesPlataforma'>

export const opcoesStatus = (Object.keys(rotulosStatus) as StatusUsuario[]).map((valor) => ({
  valor,
  texto: rotulosStatus[valor],
}))

export const opcoesTipo = (Object.keys(rotulosTipo) as TipoAcesso[]).map((valor) => ({
  valor,
  texto: rotulosTipo[valor],
}))

export interface AplicacaoAcesso {
  id: string
  slug: string
  nome: string
  status: 'ATIVO' | 'INATIVO'
}

export interface PerfilAcesso {
  id: string
  nome: string
  descricao: string | null
  status: 'ATIVO' | 'INATIVO'
}

export interface PermissaoAcesso {
  id: string
  codigo: string
  descricao: string | null
  status: 'ATIVO' | 'INATIVO'
}

export interface PermissaoPlataforma extends PermissaoAcesso {
  nome: string
}
