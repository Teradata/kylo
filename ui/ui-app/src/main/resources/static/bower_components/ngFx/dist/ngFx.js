if (typeof module !== 'undefined' && typeof exports !== 'undefined' && module.exports === exports){
  module.exports = 'ngFx';
}
(function(angular, TweenMax, TimelineMax){
  'use strict;'
/*!
* ngFx.js is a concatenation of:
* angular-animate.js and TweenMax.js
*/
/*!
* ngFx.js is a concatenation of:
* angular-animate.js and TweenMax.js
*/

/*!
* Copyright 2015 Scott Moss
*
*
* A simple, beautiful animation library for angular
* http://hendrixer.github.io
*
* By @Hendrixer
*
* Licensed under the MIT license.
*
*/

angular.module('fx.transitions',
  [
    'fx.transitions.slides',
    'fx.transitions.scales',
    'fx.transitions.rotations',
    'fx.transitions.specials',
    'fx.transitions.view'
  ]
);
angular.module('ngFx', ['fx.animations', 'fx.transitions']);





angular.module('fx.animations', [
  'fx.animations.fades',
  'fx.animations.bounces',
  'fx.animations.zooms',
  'fx.animations.rotations',
  'fx.animations.tada',
  'fx.animations.pulse'
]);

// angular.module('fx.animations.create', ['fx.animations.assist'])


//   .factory('Flip3d', ['$window', function ($window){
//     return function (effect){
//       var axis = effect.axis;
//       var flipType = 'fx-flip'+axis;
//       this.addClass = function(el, className, done){
//         var wrapper = angular.element(el.children()[0]);
//         var myDone = function(){
//           return done();
//         };
//         if(className === flipType){
//           effect.transform.ease = $window.Bounce.easeOut;
//           effect.transform.onComplete = myDone;
//           TweenMax.to(wrapper, effect.duration, effect.transform);
//         } else {
//           done();
//         }
//       };

//       this.removeClass = function(el, className, done){
//         var wrapper = angular.element(el.children()[0]);
//         var myDone = function(){
//           return done();
//         };
//         if(className === flipType){
//           effect.reset.ease = $window.Bounce.easeOut;
//           effect.reset.onComplete = myDone;
//           TweenMax.to(wrapper, effect.duration, effect.reset);
//         } else {
//           done();
//         }
//       };
//     };
//   }]);




angular.module('fx.animations.assist', [])

.factory('Assist', ['$filter', '$window', '$timeout', '$rootScope', function ($filter, $window, $timeout, $rootScope){
  return {

    emit: function(element, animation, motion){
      $rootScope.$broadcast(animation +':'+motion);
    },

    parseClassList: function(element, option){
      var ease,
          list    = element[0].classList,
          results = {trigger: false, duration: 0.3, ease: $window.Back};

      angular.forEach(list, function (className){
        if(className.slice(0,9) === 'fx-easing'){
          ease = className.slice(10);
          results.ease = $window[$filter('cap')(ease)] || $window.Elastic;
        }
        if(className === 'fx-trigger'){
          results.trigger = true;
        }
        if(className.slice(0,8) === 'fx-speed'){
          results.duration = parseInt(className.slice(9))/1000;
        }
      });

      return option ? {ease: results.ease, speed: results.duration} : results;
    },

    addTimer: function(options, element, end){
      var self = this;
      var time = options.stagger ? (options.duration * 3) * 1000 : options.duration * 1000;
      var timer = $timeout(function(){
        if(options.trigger){
          self.emit(element, options.animation, options.motion);
        }
        end();
      }, time);
      element.data(options.timeoutKey, timer);
    },

    removeTimer: function(element, timeoutKey, timer){
      $timeout.cancel(timer);
      element.removeData(timeoutKey);
    },

    timeoutKey: '$$fxTimer'
  };
}])

.filter('cap', [function(){
  return function (input){
    return input.charAt(0).toUpperCase() + input.slice(1);
  };
}]);



var timeoutKey = '$$fxtimer';
angular.module('fx.transitions.assist', [])

.factory('TransAssist', ["$timeout", function ($timeout) {
  function addTimer (el, time, done) {
    var timer = $timeout(function () {
      done();
    }, (time*1000) + 50);
    el.data(timeoutKey, timer);
  }

  function removeTimer (el) {
    var timer = el.data(timeoutKey);
    if (timer) {
      el.css('z-index', '-1');
      $timeout.cancel(timer);
      el.removeData(timeoutKey);
    }
  }

  return {
    addTimer: addTimer,
    removeTimer: removeTimer
  };
}]);



angular.module('fx.transitions.create', ['fx.transitions.assist', 'fx.animations.assist'])

.factory('SlideTransition', ['TransAssist', 'Assist', function (TransAssist, Assist) {
  var slide;

  return function (effect) {

    if (effect.from) {
      this.enter = function (el, done) {
        var customs;
        cssMixin(el);

        customs = Assist.parseClassList(el, true);
        effect.from.ease = customs.ease.easeInOut;
        effect.duration = customs.speed;

        TransAssist.addTimer(el, effect.duration, done);

        slide = new TimelineMax();

        slide.from(el, effect.duration, effect.from);
        return function (cancel) {
          if(cancel) {
            TransAssist.removeTimer(el);
          }
        };
      };

    } else if (!effect.from && effect.to) {
      this.leave = function (el, done) {
        var customs;
        cssMixin(el);

        customs = Assist.parseClassList(el, true);

        effect.to.ease = customs.ease.easeInOut;
        effect.duration = customs.speed;
        TransAssist.addTimer(el, effect.duration, done);


        slide = new TimelineMax();

        slide.to(el, effect.duration, effect.to);

        return function (cancel) {
          if(cancel) {
            TransAssist.removeTimer(el);
          }
        };
        // el.css('position', 'absolute');
        // el.css('z-index', '9999');

        // slide = new TimelineMax({onComplete: finish(done)});

        // slide.from(el, effect.duration, effect.from)
        //      .to(el, effect.duration, effect.to);

        // el.css('z-index', '9999');
        // var page = new TimelineMax({onComplete: finish(done)});
        // page.to(el, {transform: 'rotateZ(0deg)'})
        //     .to(el, 0.2, {transform: 'rotateZ(10deg)'})
        //     .to(el, 0.2, {transform: 'rotateZ(17deg)'})
        //     .to(el, 0.4, {transform: 'rotateZ(15deg)'})
        //     .to(el, 0.2, {transform: 'translateY(100%) rotateZ(17deg)'});
      };
    }
  };
}])
.factory('RotationTransition', ['TransAssist', 'Assist','$compile', function (TransAssist, Assist, $compile) {
  var rotate;
  return function (effect) {
    this[effect.when] = function (el, done) {
      var customs, wrapper;

      wrapper = $compile('<div></div>')(el.scope());

      cssMixin(el);

      css3D(wrapper, el);

      angular.element(wrapper).append(el[0].outerHTML);
      customs = Assist.parseClassList(el, true);

      effect.from.ease = customs.ease.easeOut;
      effect.duration = customs.duration;
      TransAssist.addTimer(el, effect.duration, done);
      rotate = new TimelineMax();

      rotate.from(el, 1, effect.from)
            .to(el, 1, effect.to);

      return function (cancel) {
        if(cancel) {
          TransAssist.removeTimer(el);
        }
      };
    };
  };
}]);

function cssMixin (el, z) {
  el.css('position', 'absolute');
  if (z && z === 'leave') {
    el.css('z-index', '9999px');
  } else {
    el.css('z-index', '1000px');
  }
}

function css3D (parent, view) {
  var preservve = {
    'position': 'relative',
    width: '100%',
    height: '100%',
    '-webkit-perspective': '500px',
    '-moz-perspective': '500px',
    '-o-perspective': '500px',
    'perspective': '500px'
  };

  var trans = {
    overflow: 'hidden',
    '-webkit-backface-visibility': 'hidden',
    '-moz-backface-visibility': 'hidden',
    'backface-visibility': 'hidden',
    '-webkit-transform': 'translate3d(0, 0, 0)',
    '-moz-transform': 'translate3d(0, 0, 0)',
    'transform': 'translate3d(0, 0, 0),',
   ' -webkit-transform-style': 'preserve-3d',
    '-moz-transform-style': 'preserve-3d',
    'transform-style': 'preserve-3d'
  };
  parent.css(preservve);
  view.css(trans);
}

// function calcTime  (duration, perc) {

//   return (duration * (perc/100));
// }


angular.module('fx.transitions.view', [])

.directive('fxAnimate', ["$injector", function($injector) {
  return {
    // priority: 1000,
    link: function($scope, $ele) {

      var $state, $route;

      function addAnimations(animations, ele) {
        angular.forEach(animations, function(animation, type) {
          if (type === 'ease') {
            animation = 'fx-easing-' + animation;
          }

          if (type === 'speed') {
            animation = 'fx-speed-' + animation;
          }
          ele.addClass(animation);
        });
      }

      if ($injector.has('$state')) {
        $state = $injector.get('$state');
      }

      if ($injector.has('$route')) {
        $route = $injector.get('$route');
      }


      var animations;
      if ($state && $state.current.animation && $route && $route.current){
          if ($route.current.$$route && $route.current.$$route.animation){
            throw new Error('You can only add animations on either $state or $route');
          }
      }

      if ($state) {
        animations = $state.current.animation;
      }

      if ($route && $route.current) {
        animations = $route.current.$$route.animation;
      }

      addAnimations(animations, $ele);
    }
  };
}]);


angular.module('fx.transitions.rotations', ['fx.transitions.create'])

.animation('.rotate-out-right', ['RotationTransition', function (RotationTransition) {
  var effect = {
    from: {transform: 'rotateY(15deg)', opacity: '.8'},
    to: {transform: 'scale(0.8) translateZ(-200px)', opacity: '0'},
    when: 'leave',
    duration: 0.5
  };

  return new RotationTransition(effect);
}]);


angular.module('fx.transitions.scales', ['fx.transitions.create'])

.animation('.shrink-in', ['SlideTransition', function (SlideTransition) {
  var effect = {
    from: {opacity: '0', transform: 'translateZ(0) scale(1.2)'},
    duration: 0.5
  };

  return new SlideTransition(effect);
}])
.animation('.shrink-out', ['SlideTransition', function (SlideTransition) {
  var effect = {
    to: {opacity: '0', transform: 'translateZ(0) scale(.8)'},
    duration: 0.5
  };

  return new SlideTransition(effect);
}])
.animation('.grow-in', ['SlideTransition', function (SlideTransition) {
  var effect = {
    from: {opacity: '0', transform: 'translateZ(0) scale(.8)'},
    duration: 0.5
  };

  return new SlideTransition(effect);
}])
.animation('.grow-out', ['SlideTransition', function (SlideTransition) {
  var effect = {
    to: {opacity: '0', transform: 'translateZ(0) scale(1.2)'},
    duration: 0.5
  };

  return new SlideTransition(effect);
}]);

angular.module('fx.transitions.slides', ['fx.transitions.create'])

.animation('.slide-in-left', ['SlideTransition', function (SlideTransition) {

  var effect = {
    from: { transform: 'translateZ(0) translateX(100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-out-left', ['SlideTransition', function (SlideTransition) {

  var effect = {
    to: { transform: 'translateZ(0) translateX(-100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-in-right', ['SlideTransition', function (SlideTransition) {

  var effect = {
    from: { transform: 'translateZ(0) translateX(-100%)'},
    duration: 2
  };

  return new SlideTransition(effect);

}])
.animation('.slide-out-right', ['SlideTransition', function (SlideTransition) {

  var effect = {
    to: { transform: 'translateZ(0) translateX(100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-in-down', ['SlideTransition', function (SlideTransition) {

  var effect = {
    from: { transform: 'translateZ(0) translateY(-100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-out-down', ['SlideTransition', function (SlideTransition) {

  var effect = {
    to: { transform: 'translateZ(0) translateY(100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-in-up', ['SlideTransition', function (SlideTransition) {

  var effect = {
    from: { transform: 'translateZ(0) translateY(100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-out-up', ['SlideTransition', function (SlideTransition) {

  var effect = {
    to: { transform: 'translateZ(0) translateY(-100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])



.animation('.slide-in-left-fade', ['SlideTransition', function (SlideTransition) {

  var effect = {
    from: { opacity: '0.3', transform: 'translateZ(0) translateX(100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-out-left-fade', ['SlideTransition', function (SlideTransition) {

  var effect = {
    to: { opacity: '0.3', transform: 'translateZ(0) translateX(-100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-in-right-fade', ['SlideTransition', function (SlideTransition) {

  var effect = {
    from: { opacity: '0.3', transform: 'translateZ(0) translateX(-100%)'},
    duration: 2
  };

  return new SlideTransition(effect);

}])
.animation('.slide-out-right-fade', ['SlideTransition', function (SlideTransition) {

  var effect = {
    to: { opacity: '0.3', transform: 'translateZ(0) translateX(100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-in-down-fade', ['SlideTransition', function (SlideTransition) {

  var effect = {
    from: { opacity: '0.3', transform: 'translateZ(0) translateY(-100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-out-down-fade', ['SlideTransition', function (SlideTransition) {

  var effect = {
    to: { opacity: '0.3', transform: 'translateZ(0) translateY(100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-in-up-fade', ['SlideTransition', function (SlideTransition) {

  var effect = {
    from: { opacity: '0.3', transform: 'translateZ(0) translateY(100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}])
.animation('.slide-out-up-fade', ['SlideTransition', function (SlideTransition) {

  var effect = {
    to: { opacity: '0.3', transform: 'translateZ(0) translateY(-100%)'},
    duration: 2
  };

  return new SlideTransition(effect);
}]);



angular.module('fx.transitions.specials', [])

  .animation('.fx-fall-out', function () {
  // var effect = {
  //   from: {}
  // };


    return {
      leave: function (el, done) {
        el.css('z-index', '9999');
        var page = new TimelineMax({onComplete: done});
        page.to(el, {transform: 'rotateZ(0deg)'})
            .to(el, 0.1, {transform: 'rotateZ(10deg)'})
            .to(el, 0.3, {transform: 'rotateZ(17deg)'})
            .to(el, 0.5, {transform: 'rotateZ(15deg)'})
            .to(el, 0.2, {transform: 'translateY(100%) rotateZ(17deg)'});
      }
    };
    // return new SlideTransition(effect);
  });


angular.module('fx.animations.pulse', ['fx.animations.assist'])
  .animation('.fx-pulse', ['Assist', function(Assist){
    return {
      animate: function(element, className, to, from, done){
        var effect = [

          { effect: { transform: 'scale3d(1,1,1)' }},
          { effect: { transform: 'scale3d(1.05, 1.05, 1.05)' }},
          { effect: { transform: 'scale3d(1, 1, 1)' }}

        ];

        var options = Assist.parseClassList(element);
        options.motion = 'addClass';
        options.animation = 'pulse';

        var eachTime = (options.duration / effect.length);

        Assist.addTimer(options, element, done);

        var pulse = new TimelineMax();
        angular.forEach(effect, function(step, pos){
          step.easing = options.ease.easeInOut;

          pulse = pulse.to(element, eachTime, step.effect);
        });
      }
    };
  }]);



angular.module('fx.animations.tada', ['fx.animations.assist'])
  .animation('.fx-tada', ['Assist', function(Assist){
    return {
      animate: function(element, classname, done){
        var effect = [
          // 0
          { effect: { transform: 'scale3d(1,1,1)' } },
          // 10
          { effect: { transform: 'scale3d(.9, .9, .9) rotate3d(0, 0, 1, -3deg)' } },
          // 20
          { effect: { transform: 'scale3d(.9, .9, .9) rotate3d(0, 0, 1, -3deg)' } },
          // 30
          { effect: { transform: 'scale3d(1.1, 1.1, 1.1) rotate3d(0, 0, 1, 3deg)' } },
          // 40
          { effect: { transform: 'scale3d(1.1, 1.1, 1.1) rotate3d(0, 0, 1, -3deg)' } },
          // 50
          { effect: { transform: 'scale3d(1.1, 1.1, 1.1) rotate3d(0, 0, 1, 3deg)' } },
          // 60
          { effect: { transform: 'scale3d(1.1, 1.1, 1.1) rotate3d(0, 0, 1, -3deg)' } },
          // 70
          { effect: { transform: 'scale3d(1.1, 1.1, 1.1) rotate3d(0, 0, 1, 3deg)' } },
          // 80
          { effect: { transform: 'scale3d(1.1, 1.1, 1.1) rotate3d(0, 0, 1, -3deg)' } },
          // 90
          { effect: { transform: 'scale3d(1.1, 1.1, 1.1) rotate3d(0, 0, 1, 3deg)' } },
          // 100
          { effect: { transform: ' scale3d(1, 1, 1)' } }
        ];

        var options = Assist.parseClassList(element);
        options.motion = 'addClass';
        options.animation = 'tada';

        var eachTime = (options.duration / effect.length);

        Assist.addTimer(options, element, done);

        var tada = new TimelineMax();
        angular.forEach(effect, function(step, pos){
          step.easing = options.ease.easeInOut;

          tada = tada.to(element, eachTime, step.effect);
        });
      }
    };
  }]);



/*
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  Using Angular's '.animate', all fade animations are created with javaScript.

  @BounceAnimation
    Constructor function that returns a new animation object that has all
    required methods for ngAnimate ex: this.enter(), this.leave(), etc

  @effect
    The actual animation that will be applied to the element, staggered
     first: the style to applied to the element 1/4 through the animtion
     mid: style to be applied to to the element 2/4 through the animation
     third: style to be applied to the element 3/4 through the animation
     end: style to be applied to the element when it's complete
     animation: the name of the animtion for the eventing system
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

*/

angular.module('fx.animations.bounces', ['fx.animations.bounces.factory'])

.animation('.fx-bounce-normal', ['BounceAnimation', function (BounceAnimation){
  var effect = {
    first: {opacity: 0, transform: 'scale(.3)'},
    mid: {opacity: 1, transform: 'scale(1.05)'},
    third: {transform: 'scale(.9)'},
    end: {opacity: 1, transform: 'scale(1)'},
    animation: 'bounce-normal'
  };

  return new BounceAnimation(effect);
}])

.animation('.fx-bounce-down', ['BounceAnimation', function (BounceAnimation){
  var effect = {
    first: {opacity: 0, transform: 'translateY(-2000px)'},
    mid: {opacity: 1, transform: 'translateY(30px)'},
    third: {transform: 'translateY(-10px)'},
    end: {transform: 'translateY(0)'},
    animation: 'bounce-down'
  };


  return new BounceAnimation(effect);
}])

.animation('.fx-bounce-left', ['BounceAnimation', function (BounceAnimation){
  var effect = {
    first: {opacity: 0,  transform: 'translateX(-2000px)'},
    mid: {opacity: 1, transform: 'translateX(30px)'},
    third: {transform: 'translateX(-10px)'},
    end: {transform: 'translateX(0)'},
    animation: 'bounce-left'
  };

  return new BounceAnimation(effect);
}])

.animation('.fx-bounce-up', ['BounceAnimation', function (BounceAnimation) {
  var effect = {
    first: {opacity: 0,   transform: 'translateY(2000px)'},
    mid: {opacity: 1, transform: 'translateY(-30px)'},
    third: {transform: 'translateY(10px)'},
    end: {transform: 'translateY(0)'},
    animation: 'bounce-up'
  };
  return new BounceAnimation(effect);
}])

.animation('.fx-bounce-right', ['BounceAnimation', function (BounceAnimation) {
  var effect = {
    first: {opacity: 0,   transform: 'translateX(2000px)'},
    mid: {opacity: 1, transform: 'translateX(-30px)'},
    third: {transform: 'translateX(10px)'},
    end: {transform: 'translateX(0)'},
    animation: 'bounce-right'
  };
  return new BounceAnimation(effect);
}]);


angular.module('fx.animations.bounces.factory', ['fx.animations.assist'])
  .factory('BounceAnimation', ['$timeout', '$window', 'Assist', function ($timeout, $window, Assist){
  return function (effect){
    var start       = effect.first,
        mid         = effect.mid,
        third       = effect.third,
        end         = effect.end,
        fx_type     = effect.animation,
        startTime   = 0.1;

    this.enter = function(element, done){
      var options = Assist.parseClassList(element);
      options.motion = 'enter';
      options.animation = fx_type;
      options.timeoutKey = Assist.timeoutKey;
      options.stagger = true;
      Assist.addTimer(options, element, done);
      var enter = new TimelineMax();
      enter.to(element, 0.01, start);
      enter.to(element, options.duration, mid);
      enter.to(element, options.duration, third);
      enter.to(element, options.duration, end);
      return function (canceled){
        if(canceled){
          var timer = element.data(Assist.timeoutKey);
          if(timer){
            Assist.removeTimer(element, Assist.timeoutKey, timer);
          }
        }
      };
    };

    this.leave = function(element, done){
      var options = Assist.parseClassList(element);
      options.motion = 'leave';
      options.animation = fx_type;
      options.timeoutKey = Assist.timeoutKey;
      options.stagger = true;
      Assist.addTimer(options, element, done);
      var leave = new TimelineMax();
      leave.to(element, startTime, end);
      leave.to(element, options.duration, third);
      leave.to(element, options.duration, mid);
      leave.to(element, options.duration, start);
      return function (canceled){
        if(canceled){
          var timer = element.data(timeoutKey);
          if(timer){
            Assist.removeTimer(element, Assist.timeoutKey, timer);
          }
        }
      };
    };

    this.move = this.enter;

    this.addClass = function(element, className, done){
      if(className){
        var options = Assist.parseClassList(element);
        options.motion = 'enter';
        options.animation = fx_type;
        options.timeoutKey = Assist.timeoutKey;
        Assist.addTimer(options, element, done);
        var bac = new TimelineMax();
        bac.to(element, startTime, end);
        bac.to(element, options.duration, third);
        bac.to(element, options.duration, mid);
        bac.to(element, options.duration, start);
        return function (canceled){
          if(canceled){
            var timer = element.data(timeoutKey);
            if(timer){
              Assist.removeTimer(element, Assist.timeoutKey, timer);
            }
          }
        };
      } else {
        done();
      }
    };

    this.removeClass = function(element, className, done){
      if(className){
        var options = Assist.parseClassList(element);
        options.motion = 'leave';
        options.animation = fx_type;
        options.timeoutKey = Assist.timeoutKey;
        var rc = new TimelineMax();
        rc.to(element, startTime, start);
        rc.to(element, options.duration, mid);
        rc.to(element, options.duration, third);
        rc.to(element, options.duration, end);
        return function (canceled){
          if(canceled){
            var timer = element.data(timeoutKey);
            if(timer){
              Assist.removeTimer(element, Assist.timeoutKey, timer);
            }
          }
        };
      } else {
        done();
      }
    };
  };
}])


/*
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  Using Angular's '.animate', all fade animations are created with javaScript.

  @FadeAnimation
    Constructor function that returns a new animation object that has all
    required methods for ngAnimate ex: this.enter(), this.leave(), etc

  @effect
    The actual animation that will be applied to the element
     enter: style to be applied when angular triggers the enter event
     leave: style to be applied when angular triggers the leave event
     inverse: style to be appiled to offset the enter event
     animation: the name of the animtion for the eventing system
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

*/

angular.module('fx.animations.fades', ['fx.animations.fades.factory'])

.animation('.fx-fade-normal', ['FadeAnimation', function (FadeAnimation){
  var effect = {
    enter: {opacity: 1},
    leave: {opacity: 0},
    animation: 'fade-normal'
  };
  return new FadeAnimation(effect);
}])


.animation('.fx-fade-down', ['FadeAnimation', function (FadeAnimation){
  var effect = {
    enter: {opacity: 1, transform: 'translateY(0)'},
    leave: {opacity: 0, transform: 'translateY(-20px)'},
    inverse: {opacity: 0, transform: 'translateY(20px)'},
    animation: 'fade-down'
  };
  return new FadeAnimation(effect);
}])

.animation('.fx-fade-down-big', ['FadeAnimation', function (FadeAnimation){
  var effect = {
    enter: {opacity: 1, transform: 'translateY(0)'},
    leave: {opacity: 0, transform: 'translateY(-2000px)'},
    inverse: {opacity: 0, transform: 'translateY(2000px)'},
    animation: 'fade-down-big'
  };
  return new FadeAnimation(effect);
}])

.animation('.fx-fade-left', ['FadeAnimation', function (FadeAnimation){
  var effect = {
    enter: {opacity: 1, transform: 'translateX(0)'},
    leave: {opacity: 0, transform: 'translateX(-20px)'},
    inverse: {opacity: 0, transform: 'translateX(20px)'},
    animation: 'fade-left'
  };
  return new FadeAnimation(effect);
}])

.animation('.fx-fade-left-big', ['FadeAnimation', function (FadeAnimation){
  var effect = {
    enter: {opacity: 1, transform: 'translateX(0)'},
    leave: {opacity: 0, transform: 'translateX(-2000px)'},
    inverse: {opacity: 0, transform: 'translateX(2000px)'},
    animation: 'fade-left-big'
  };

  return new FadeAnimation(effect);
}])

.animation('.fx-fade-right', ['FadeAnimation', function (FadeAnimation){
  var effect = {
    enter: {opacity: 1, transform: 'translateX(0)'},
    leave: {opacity: 0, transform:'translateX(20px)'},
    inverse: {opacity: 0, transform: 'translateX(-20px)'},
    animation: 'fade-right'
  };

  return new FadeAnimation(effect);
}])

.animation('.fx-fade-right-big', ['FadeAnimation', function (FadeAnimation){
  var effect = {
    enter: {opacity: 1, transform: 'translateX(0)'},
    leave: {opacity: 0, transform:'translateX(2000px)'},
    inverse: {opacity: 0, transform: 'translateX(-2000px)'},
    animation: 'fade-right-big'
  };

  return new FadeAnimation(effect);
}])

.animation('.fx-fade-up', ['FadeAnimation', function (FadeAnimation){
  var effect = {
    enter: {opacity: 1, transform: 'translateY(0)'},
    leave: {opacity: 0, transform:'translateY(20px)'},
    inverse: {opacity: 0, transform: 'translateY(-20px)'},
    animation: 'fade-up'
  };

  return new FadeAnimation(effect);
}])

.animation('.fx-fade-up-big', ['FadeAnimation', function (FadeAnimation){
  var effect = {
    enter: {opacity: 1, transform: 'translateY(0)'},
    leave: {opacity: 0, transform:'translateY(2000px)'},
    inverse: {opacity: 0, transform: 'translateY(-2000px)'},
    animation: 'fade-up-big'
  };

  return new FadeAnimation(effect);
}])

.animation('.fx-fade-overlay', ['FadeAnimation', function(FadeAnimation) {
  var effect = {
    enter: {opacity: 0.7},
    leave: {opacity: 0},
    inverse: {opacity: 0},
    animation: 'fade-overlay'
  };

  return new FadeAnimation(effect);
}]);


angular.module('fx.animations.fades.factory', ['fx.animations.assist'])
  .factory('FadeAnimation', ['$timeout', '$window', 'Assist', function ($timeout, $window, Assist){
  return function (effect){
    var inEffect        = effect.enter,
        outEffect       = effect.leave,
        outEffectLeave  = effect.inverse || effect.leave,
        fx_type         = effect.animation;

    this.enter = function(element, done){
      var options = Assist.parseClassList(element);
      options.motion = 'enter';
      options.animation = fx_type;
      options.timeoutKey = Assist.timeoutKey;
      Assist.addTimer(options, element, done);
      inEffect.ease = options.ease.easeOut;
      TweenMax.set(element, outEffect);
      TweenMax.to(element, options.duration, inEffect);
      return function (canceled){
        var timer = element.data(timeoutKey);
        if(canceled){
          if(timer){
            Assist.removeTimer(element, Assist.timeoutKey, timer);
          }
        }
      };
    };

    this.leave = function(element, done){
      var options = Assist.parseClassList(element);
      options.motion = 'leave';
      options.animation = fx_type;
      options.timeoutKey = Assist.timeoutKey;
      Assist.addTimer(options, element, done);
      outEffectLeave.ease = options.ease.easeIn;
      TweenMax.set(element, inEffect);
      TweenMax.to(element, options.duration, outEffectLeave);
      return function (canceled){
        var timer = element.data(Assist.timeoutKey);
        if(canceled){
          if(timer){
            Assist.removeTimer(element, Assist.timeoutKey, timer);
          }
        }
      };
    };

    this.move = this.enter;

    this.addClass = function(element, className, done){
      if(className){
        var options = Assist.parseClassList(element);
        options.motion = 'enter';
        options.animation = fx_type;
        options.timeoutKey = Assist.timeoutKey;
        Assist.addTimer(options, element, done);
        TweenMax.to(element, options.duration, outEffectLeave);
        return function (canceled){
          if(canceled){
            var timer = element.data(timeoutKey);
            if(timer){
              Assist.removeTimer(element, Assist.timeoutKey, timer);
            }
          }
        };
      } else {
        done();
      }
    };

    this.removeClass = function(element, className, done){
      if(className){
        var options = Assist.parseClassList(element);
        options.motion = 'leave';
        options.animation = fx_type;
        options.timeoutKey = Assist.timeoutKey;
        TweenMax.set(element, outEffect);
        TweenMax.to(element, options.duration, inEffect);
        return function (canceled){
          if(canceled){
            var timer = element.data(timeoutKey);
            if(timer){
              Assist.removeTimer(element, Assist.timeoutKey, timer);
            }
          }
        };
      } else {
        done();
      }
    };
  };
}]);


/*
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  Using Angular's '.animate', all fade animations are created with javaScript.

  @RotateAnimation
    Constructor function that returns a new animation object that has all
    required methods for ngAnimate ex: this.enter(), this.leave(), etc

  @effect
    The actual animation that will be applied to the element, staggered
     first: the style to applied to the element 1/4 through the animtion
     mid: style to be applied to to the element 2/4 through the animation
     third: style to be applied to the element 3/4 through the animation
     end: style to be applied to the element when it's complete
     animation: the name of the animtion for the eventing system
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

*/

angular.module('fx.animations.rotations', ['fx.animations.rotations.factory'])

.animation('.fx-rotate-counterclock', ['RotateAnimation', function(RotateAnimation){
  var effect = {
    start: {opacity: 0, transformOrigin: 'center center', transform: 'rotate(-200deg)'},
    end: {opacity: 1, transformOrigin: 'center center', transform: 'rotate(0)'},
    inverse: {opacity: 0, transformOrigin: 'center center', transform: 'rotate(200deg)'},
    animation: 'rotate-counterclock'
  };
  return new RotateAnimation(effect);
}])

.animation('.fx-rotate-clock', ['RotateAnimation', function(RotateAnimation){
  var effect = {
    start: {opacity: 0, transformOrigin: 'center center', transform: 'rotate(200deg)'},
    end: {opacity: 1, transformOrigin: 'center center', transform: 'rotate(0)'},
    inverse: {opacity: 0, transformOrigin: 'center center', transform: 'rotate(-200deg)'},
    animation: 'rotate-clock'
  };
  return new RotateAnimation(effect);
}])
.animation('.fx-rotate-clock-left', ['RotateAnimation', function(RotateAnimation){
  var effect = {
    start: {opacity: 0, transformOrigin: 'left bottom', transform: 'rotate(-90deg)'},
    end: {opacity: 1, transformOrigin: 'left bottom', transform: 'rotate(0)'},
    inverse: {opacity: 0, transformOrigin: 'left bottom', transform: 'rotate(90deg)'},
    animation: 'rotate-clock-left'
  };
  return new RotateAnimation(effect);
}])
.animation('.fx-rotate-counterclock-right', ['RotateAnimation', function(RotateAnimation){
  var effect = {
    start: {opacity: 0, transformOrigin: 'right bottom', transform: 'rotate(90deg)'},
    end: {opacity: 1, transformOrigin: 'right bottom', transform: 'rotate(0)'},
    inverse: {opacity: 0, transformOrigin: 'right bottom', transform: 'rotate(-90deg)'},
    animation: 'rotate-counterclock-right'
  };
  return new RotateAnimation(effect);
}])
.animation('.fx-rotate-counterclock-up', ['RotateAnimation', function(RotateAnimation){
  var effect = {
    start: {opacity: 0, transformOrigin: 'left bottom', transform: 'rotate(90deg)'},
    end: {opacity: 1, transformOrigin: 'left bottom', transform: 'rotate(0)'},
    inverse: {opacity: 0, transformOrigin: 'left bottom', transform: 'rotate(-90deg)'},
    animation: 'rotate-counterclock-up'
  };
  return new RotateAnimation(effect);
}])
.animation('.fx-rotate-clock-up', ['RotateAnimation', function(RotateAnimation){
  var effect = {
    start: {opacity: 0, transformOrigin: 'right bottom', transform: 'rotate(-90deg)'},
    end: {opacity: 1, transformOrigin: 'right bottom', transform: 'rotate(0)'},
    inverse: {opacity: 0, transformOrigin: 'right bottom', transform: 'rotate(90deg)'},
    animation: 'rotate-clock-up'
  };
  return new RotateAnimation(effect);
}]);



angular.module('fx.animations.rotations.factory', ['fx.animations.assist'])
  .factory('RotateAnimation', ['$timeout', '$window', 'Assist', function ($timeout, $window, Assist){
    return function (effect){
      var start       = effect.start,
          end         = effect.end,
          leaveEnd    = effect.inverse,
          fx_type     = effect.animation;

      this.enter = function(element, done){
        var options = Assist.parseClassList(element);
            options.motion = 'enter';
            options.animation = fx_type;
            options.timeoutKey = Assist.timeoutKey;

        end.ease = options.ease.easeOut;
        Assist.addTimer(options, element, done);
        TweenMax.set(element, start);
        TweenMax.to(element, options.duration, end);
        return function (canceled){
          if(canceled){
            var timer = element.data(Assist.timeoutKey);
            if(timer){
              Assist.removeTimer(element, Assist.timeoutKey, timer);
            }
          }
        };
      };

      this.leave = function(element, done){
        var options = Assist.parseClassList(element);
            options.motion = 'leave';
            options.animation = fx_type;
            options.timeoutKey = Assist.timeoutKey;

        leaveEnd.ease = options.ease.easeIn;
        Assist.addTimer(options, element, done);
        TweenMax.set(element, end);
        TweenMax.to(element, options.duration, leaveEnd);
        return function (canceled){
          if(canceled){
            var timer = element.data(timeoutKey);
            if(timer){
              Assist.removeTimer(element, Assist.timeoutKey, timer);
            }
          }
        };
      };

      this.move = this.enter;

      this.addClass = function(element, className, done){
        if(className){
          var options = Assist.parseClassList(element);
          options.motion = 'enter';
          options.animation = fx_type;
          options.timeoutKey = Assist.timeoutKey;
          Assist.addTimer(options, element, done);
          TweenMax.set(element, end);
          TweenMax.to(element, options.duration, start);
          return function (canceled){
            if(canceled){
              var timer = element.data(timeoutKey);
              if(timer){
                Assist.removeTimer(element, Assist.timeoutKey, timer);
              }
            }
          };
        } else {
          done();
        }
      };

       this.removeClass = function(element, className, done){
        if(className){
          var options = Assist.parseClassList(element);
          options.motion = 'enter';
          options.animation = fx_type;
          options.timeoutKey = Assist.timeoutKey;
          Assist.addTimer(options, element, done);
          TweenMax.set(element, start);
          TweenMax.to(element, options.duration, end);
          return function (canceled){
            if(canceled){
              var timer = element.data(timeoutKey);
              if(timer){
                Assist.removeTimer(element, Assist.timeoutKey, timer);
              }
            }
          };
        } else {
          done();
        }
      };
    };
  }])

/*
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  Using Angular's '.animate', all fade animations are created with javaScript.

  @RotateAnimation
    Constructor function that returns a new animation object that has all
    required methods for ngAnimate ex: this.enter(), this.leave(), etc

  @effect
    The actual animation that will be applied to the element, staggered
     first: the style to applied to the element 1/4 through the animtion
     mid: style to be applied to to the element 2/4 through the animation
     third: style to be applied to the element 3/4 through the animation
     end: style to be applied to the element when it's complete
     animation: the name of the animtion for the eventing system
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

*/

angular.module('fx.animations.zooms', ['fx.animations.zooms.factory'])

.animation('.fx-zoom-normal', ['ZoomAnimation', function (ZoomAnimation){
  var effect = {
    start: {opacity: 0, transform: 'scale(.3)'},
    end: {opacity: 1, transform: 'scale(1)'},
    animation: 'zoom-normal'
  };

  return new ZoomAnimation(effect);
}])

.animation('.fx-zoom-down', ['ZoomAnimation', function (ZoomAnimation){
  var effect = {
    start: {opacity: 0, transform: 'scale(.1) translateY(-2000px)'},
    end: {opacity: 1, transform: 'scale(1) translateY(0)'},
    animation: 'zoom-down'
  };

  return new ZoomAnimation(effect);
}])

.animation('.fx-zoom-up', ['ZoomAnimation', function (ZoomAnimation){
  var effect = {
    start: {opacity: 0, transform: "scale(.1) translateY(2000px)"},
    end: {opacity: 1, transform: "scale(1) translateY(0)"},
    animation: 'zoom-up'
  };

  return new ZoomAnimation(effect);
}])

.animation('.fx-zoom-right', ['ZoomAnimation', function (ZoomAnimation){
  var effect = {
    start: {opacity: 0, transform: 'scale(.1) translateX(2000px)'},
    end: {opacity: 1, transform: 'scale(1) translateX(0)'},
    animation: 'zoom-right'
  };

  return new ZoomAnimation(effect);
}])

.animation('.fx-zoom-left', ['ZoomAnimation', function (ZoomAnimation){
  var effect = {
    start: {opacity: 0, transform: 'scale(.1) translateX(-2000px)'},
    end: {opacity: 1, transform: 'scale(1) translateX(0)'},
    animation: 'zoom-left'
  };

  return new ZoomAnimation(effect);
}]);

angular.module('fx.animations.zooms.factory', ['fx.animations.assist'])
  .factory('ZoomAnimation', ['$timeout', '$window', 'Assist', function ($timeout, $window, Assist){
    return function (effect){
      var start       = effect.start,
          end         = effect.end,
          fx_type     = effect.animation;

      this.enter = function(element, done){
        var options             = Assist.parseClassList(element);
            options.motion      = 'enter';
            options.animation   = fx_type;
            options.timeoutKey  = Assist.timeoutKey;
        end.ease = options.ease.easeOut;
        Assist.addTimer(options, element, done);
        TweenMax.set(element, start);
        TweenMax.to(element, options.duration, end);
        return function (canceled){
          if(canceled){
            var timer = element.data(Assist.timeoutKey);
            if(timer){
              Assist.removeTimer(element, Assist.timeoutKey, timer);
            }
          }
        };
      };

      this.leave = function(element, done){
        var options             = Assist.parseClassList(element);
            options.motion      = 'leave';
            options.animation   = fx_type;
            options.timeoutKey  = Assist.timeoutKey;

        start.ease = options.ease.easeIn;
        Assist.addTimer(options, element, done);
        TweenMax.set(element, end);
        TweenMax.to(element, options.duration, start);
        return function (canceled){
          if(canceled){
            var timer = element.data(timeoutKey);
            if(timer){
              Assist.removeTimer(element, Assist.timeoutKey, timer);
            }
          }
        };
      };

      this.move = this.enter;

      this.removeClass = function(element, className, done){
        if(className === 'ng-hide'){
          var options = Assist.parseClassList(element);
          options.motion = 'leave';
          options.animation = fx_type;
          options.timeoutKey = Assist.timeoutKey;
          Assist.addTimer(options, element, done);
          TweenMax.set(element, start);
          TweenMax.to(element, options.duration, end);
          return function (canceled){
            if(canceled){
              var timer = element.data(timeoutKey);
              if(timer){
                Assist.removeTimer(element, Assist.timeoutKey, timer);
              }
            }
          };
        } else {
          done();
        }
      };

      this.addClass = function(element, className, done){
        if(className === 'ng-hide'){
          var options = Assist.parseClassList(element);
          options.motion = 'enter';
          options.animation = fx_type;
          options.timeoutKey = Assist.timeoutKey;
          Assist.addTimer(options, element, done);
          TweenMax.set(element, end);
          TweenMax.to(element, options.duration, start);
          return function (canceled){
            if(canceled){
              var timer = element.data(timeoutKey);
              if(timer){
                Assist.removeTimer(element, Assist.timeoutKey, timer);
              }
            }
          };
        } else {
          done();
        }
      };
    };
  }])

}(angular, TweenMax, TimelineMax));
