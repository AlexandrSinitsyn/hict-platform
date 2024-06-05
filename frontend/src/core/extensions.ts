import { FileType, Role } from '@types';

export const roleNames: string[] = Object.values(Role).filter(
    (value) => typeof value === 'string'
) as string[];

export function getRole(role: Role): string {
    return roleNames[role];
}

export function roleOrNull(role: Role | null | undefined): string | undefined {
    return !role ? undefined : getRole(role);
}

export function isAtLeast(role: Role | null | undefined, min: Role): boolean {
    return !role ? false : +Role[role] >= min;
}

export function fileType(value: FileType): keyof typeof FileType {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    return Object.entries(FileType).find(([_, val]) => val === value)?.[0];
}
