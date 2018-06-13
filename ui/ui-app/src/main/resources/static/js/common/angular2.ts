import { AboutKyloService } from './about-kylo/AboutKyloService';

export function aboutKyloServiceFactory(i: angular.auto.IInjectorService) {
  return i.get("AboutKyloService");
}

export const aboutKyloServiceProvider = {
  provide: AboutKyloService,
  useFactory: aboutKyloServiceFactory,
  deps: ['$injector']
};

