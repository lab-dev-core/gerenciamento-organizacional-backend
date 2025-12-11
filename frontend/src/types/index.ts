// Enums
export enum LifeStage {
  ASPIRANCY = 'ASPIRANCY',
  DISCIPLESHIP = 'DISCIPLESHIP',
  DISCIPLESHIP_IN_MISSION = 'DISCIPLESHIP_IN_MISSION',
  CONSECRATED_PERMANENT = 'CONSECRATED_PERMANENT',
  VOCATIONAL = 'VOCATIONAL',
  MISSION_ASSISTANT = 'MISSION_ASSISTANT',
}

export enum DocumentType {
  PERSONAL = 'PERSONAL',
  STAGE_SPECIFIC = 'STAGE_SPECIFIC',
  LOCATION_SPECIFIC = 'LOCATION_SPECIFIC',
  GENERAL = 'GENERAL',
}

export enum AccessLevel {
  PRIVATE = 'PRIVATE',
  RESTRICTED = 'RESTRICTED',
  STAGE_BASED = 'STAGE_BASED',
  LOCATION_BASED = 'LOCATION_BASED',
  PUBLIC = 'PUBLIC',
}

export enum MeetingStatus {
  SCHEDULED = 'SCHEDULED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  RESCHEDULED = 'RESCHEDULED',
}

export enum MeetingType {
  SPIRITUAL_DIRECTION = 'SPIRITUAL_DIRECTION',
  VOCATIONAL_ACCOMPANIMENT = 'VOCATIONAL_ACCOMPANIMENT',
  FORMATIVE_EVALUATION = 'FORMATIVE_EVALUATION',
  COMMUNITY_INTEGRATION = 'COMMUNITY_INTEGRATION',
  PERSONAL_DEVELOPMENT = 'PERSONAL_DEVELOPMENT',
  MINISTERIAL_FORMATION = 'MINISTERIAL_FORMATION',
}

export enum VisibilityLevel {
  PRIVATE = 'PRIVATE',
  SHARED_SPECIFIC = 'SHARED_SPECIFIC',
  SHARED_ROLE = 'SHARED_ROLE',
  COORDINATION = 'COORDINATION',
}

export enum TenantStatus {
  TRIAL = 'TRIAL',
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  CANCELLED = 'CANCELLED',
}

// User types
export interface User {
  id: number;
  username: string;
  name: string;
  email?: string;
  city?: string;
  state?: string;
  age?: number;
  phone?: string;
  education?: string;
  lifeStage?: LifeStage;
  role?: Role;
  missionLocation?: MissionLocation;
  mentor?: User;
  formativeStages?: FormativeStage[];
}

export interface Role {
  id: number;
  name: string;
  description?: string;
  canManageUsers: boolean;
  canManageRoles: boolean;
  canManageStages: boolean;
  canManageDocuments: boolean;
}

export interface MissionLocation {
  id: number;
  name: string;
  description?: string;
  city?: string;
  state?: string;
  country?: string;
  address?: string;
  postalCode?: string;
  coordinator?: User;
  users?: User[];
}

export interface FormativeStage {
  id: number;
  name: string;
  startDate?: string;
  endDate?: string;
  durationMonths?: number;
  user?: User;
}

// Document types
export interface FormativeDocument {
  id: number;
  title: string;
  content: string;
  keywords?: string;
  documentType?: DocumentType;
  accessLevel?: AccessLevel;
  creationDate?: string;
  lastModifiedDate?: string;
  author?: User;
  categories?: DocumentCategory[];
  allowedUsers?: User[];
  allowedRoles?: Role[];
  allowedStages?: LifeStage[];
  allowedLocations?: MissionLocation[];
  hasAttachment?: boolean;
}

export interface DocumentCategory {
  id: number;
  name: string;
  description?: string;
  parentCategory?: DocumentCategory;
  subcategories?: DocumentCategory[];
  documents?: FormativeDocument[];
}

export interface DocumentReadingProgress {
  id: number;
  document?: FormativeDocument;
  user?: User;
  progressPercentage: number;
  completed: boolean;
  firstViewDate?: string;
  lastViewDate?: string;
  completedDate?: string;
  userNotes?: string;
}

// Meeting types
export interface FollowUpMeeting {
  id: number;
  title: string;
  scheduledDate: string;
  actualDate?: string;
  content?: string;
  objectives?: string;
  discussionPoints?: string;
  commitments?: string;
  nextSteps?: string;
  mentorNotes?: string;
  status: MeetingStatus;
  meetingType: MeetingType;
  visibilityLevel: VisibilityLevel;
  mentor: User;
  mentee: User;
  sharedWithUsers?: User[];
  sharedWithRoles?: Role[];
}

export interface MeetingStatistics {
  totalMeetings: number;
  completedMeetings: number;
  scheduledMeetings: number;
  cancelledMeetings: number;
  meetingsByType: { [key: string]: number };
  averageMeetingsPerMentee: number;
}

// Auth types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  username: string;
  name: string;
  roleName: string;
}

export interface AuthUser {
  id: number;
  username: string;
  name: string;
  roleName: string;
}

// Request types
export interface CreateUserRequest {
  username: string;
  password: string;
  name: string;
  email?: string;
  city?: string;
  state?: string;
  age?: number;
  phone?: string;
  education?: string;
  lifeStage?: LifeStage;
  roleId?: number;
  missionLocationId?: number;
  mentorId?: number;
}

export interface CreateRoleRequest {
  name: string;
  description?: string;
  canManageUsers: boolean;
  canManageRoles: boolean;
  canManageStages: boolean;
  canManageDocuments: boolean;
}

export interface CreateLocationRequest {
  name: string;
  description?: string;
  city?: string;
  state?: string;
  country?: string;
  address?: string;
  postalCode?: string;
  coordinatorId?: number;
}

export interface CreateStageRequest {
  name: string;
  startDate?: string;
  endDate?: string;
  durationMonths?: number;
}

export interface CreateDocumentRequest {
  title: string;
  content: string;
  keywords?: string;
  documentType?: DocumentType;
  accessLevel?: AccessLevel;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
  parentCategoryId?: number;
}

export interface CreateMeetingRequest {
  title: string;
  scheduledDate: string;
  actualDate?: string;
  content?: string;
  objectives?: string;
  discussionPoints?: string;
  commitments?: string;
  nextSteps?: string;
  mentorNotes?: string;
  meetingType: MeetingType;
  visibilityLevel: VisibilityLevel;
  menteeId: number;
}

export interface UpdateReadingProgressRequest {
  progressPercentage: number;
  completed: boolean;
  userNotes?: string;
}

// Search types
export interface DocumentSearchParams {
  title?: string;
  authorId?: number;
  documentType?: DocumentType;
  accessLevel?: AccessLevel;
  lifeStage?: LifeStage;
  locationId?: number;
  startDate?: string;
  endDate?: string;
  keyword?: string;
}

// Tenant types (for multi-tenancy)
export interface Tenant {
  id: number;
  name: string;
  subdomain: string;
  status: TenantStatus;
}

export interface Plan {
  id: number;
  name: string;
  description?: string;
  price: number;
  maxUsers: number;
  maxDocuments: number;
  maxStorageMb: number;
}

export interface Subscription {
  id: number;
  tenant: Tenant;
  plan: Plan;
  startDate: string;
  endDate?: string;
  status: string;
}
