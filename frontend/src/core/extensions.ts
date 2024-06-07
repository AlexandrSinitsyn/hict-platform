import { FileType } from '@types';

export function fileType(value: FileType): keyof typeof FileType {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    return Object.entries(FileType).find(([_, val]) => val === value)?.[0];
}
