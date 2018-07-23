export interface Category {
    id: string;
    name: string;
    systemName: string;
    description: string
    createFeed?: boolean;
    icon?: string;
    iconColor?: string;
}