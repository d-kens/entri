import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

export interface EventItem {
  id: string;
  title: string;
  day: string;
  month: string;
  time: string;
  venue: string;
  city: string;
  category: string;
  price: number | null;
  gradient: string;
  emoji: string;
  tag?: string;
}

const MOCK_EVENTS: EventItem[] = [
  {
    id: '1',
    title: 'Afro Fusion Night',
    day: '07', month: 'Jun',
    time: '8:00 PM',
    venue: 'Alchemist Bar', city: 'Nairobi',
    category: 'Music',
    price: 1500,
    gradient: 'linear-gradient(135deg, #2D1B4E 0%, #7C3AED 100%)',
    emoji: '🎵',
    tag: 'Selling fast',
  },
  {
    id: '2',
    title: 'Nairobi Tech Summit 2026',
    day: '13', month: 'Jun',
    time: '9:00 AM',
    venue: 'Sarit Expo Centre', city: 'Nairobi',
    category: 'Tech',
    price: 3000,
    gradient: 'linear-gradient(135deg, #0D3B5E 0%, #0EA5E9 100%)',
    emoji: '💻',
  },
  {
    id: '3',
    title: 'Laugh Out Loud Comedy Show',
    day: '14', month: 'Jun',
    time: '7:00 PM',
    venue: 'Kenya National Theatre', city: 'Nairobi',
    category: 'Comedy',
    price: 1000,
    gradient: 'linear-gradient(135deg, #7C2D12 0%, #EA580C 100%)',
    emoji: '😂',
    tag: 'New',
  },
  {
    id: '4',
    title: 'Nairobi Marathon 2026',
    day: '22', month: 'Jun',
    time: '6:00 AM',
    venue: 'Uhuru Park', city: 'Nairobi',
    category: 'Sports',
    price: 500,
    gradient: 'linear-gradient(135deg, #064E3B 0%, #10B981 100%)',
    emoji: '🏃',
  },
  {
    id: '5',
    title: 'Food & Wine Festival',
    day: '28', month: 'Jun',
    time: '12:00 PM',
    venue: 'Ngong Racecourse', city: 'Nairobi',
    category: 'Food',
    price: 2000,
    gradient: 'linear-gradient(135deg, #713F12 0%, #F59E0B 100%)',
    emoji: '🍷',
    tag: 'Popular',
  },
  {
    id: '6',
    title: 'Blankets & Wine',
    day: '29', month: 'Jun',
    time: '2:00 PM',
    venue: 'Kasarani Grounds', city: 'Nairobi',
    category: 'Music',
    price: 2500,
    gradient: 'linear-gradient(135deg, #1E3A5F 0%, #6366F1 100%)',
    emoji: '🎶',
  },
  {
    id: '7',
    title: 'Mombasa Beach Carnival',
    day: '05', month: 'Jul',
    time: '10:00 AM',
    venue: 'Nyali Beach', city: 'Mombasa',
    category: 'Arts',
    price: null,
    gradient: 'linear-gradient(135deg, #0E4D5C 0%, #06B6D4 100%)',
    emoji: '🎪',
    tag: 'Free',
  },
  {
    id: '8',
    title: 'StartUp Grind Nairobi',
    day: '10', month: 'Jul',
    time: '6:00 PM',
    venue: 'iHub, Kilimani', city: 'Nairobi',
    category: 'Tech',
    price: 500,
    gradient: 'linear-gradient(135deg, #312E81 0%, #8B5CF6 100%)',
    emoji: '🚀',
  },
  {
    id: '9',
    title: 'Gospel Concert Live',
    day: '12', month: 'Jul',
    time: '5:00 PM',
    venue: 'KICC Grounds', city: 'Nairobi',
    category: 'Music',
    price: 800,
    gradient: 'linear-gradient(135deg, #4A1942 0%, #DB2777 100%)',
    emoji: '🙌',
  },
];

interface CategoryConfig { label: string; icon: string; }

const CATEGORY_CONFIG: CategoryConfig[] = [
  { label: 'All',     icon: 'apps' },
  { label: 'Music',   icon: 'music_note' },
  { label: 'Tech',    icon: 'laptop' },
  { label: 'Comedy',  icon: 'sentiment_very_satisfied' },
  { label: 'Sports',  icon: 'sports' },
  { label: 'Food',    icon: 'restaurant' },
  { label: 'Arts',    icon: 'palette' },
];

@Component({
  selector: 'app-events-listing',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  templateUrl: './events-listing.html',
  styleUrl: './events-listing.css',
})
export class EventsListing {
  searchQuery = signal('');
  activeCategory = signal('All');

  readonly categoryConfig = CATEGORY_CONFIG;

  filteredEvents = computed(() => {
    const q = this.searchQuery().toLowerCase();
    const cat = this.activeCategory();
    return MOCK_EVENTS.filter(e => {
      const matchesSearch = !q
        || e.title.toLowerCase().includes(q)
        || e.venue.toLowerCase().includes(q)
        || e.city.toLowerCase().includes(q);
      const matchesCategory = cat === 'All' || e.category === cat;
      return matchesSearch && matchesCategory;
    });
  });

  featuredEvent = computed(() => this.filteredEvents()[0] ?? null);
  gridEvents    = computed(() => this.filteredEvents().slice(1));

  setCategory(cat: string) { this.activeCategory.set(cat); }

  onSearch(event: Event) {
    this.searchQuery.set((event.target as HTMLInputElement).value);
  }
}
