import './styles.css';
import { LayoutView } from 'backbone.marionette';
import { region, className } from '../../decorators';
import template from './AppLayout.hbs';
import SideNav from '../../components/side-nav/SideNavView';

@className('app')
class AppLayout extends LayoutView {
    template = template;

    @region('.app__content')
    content;
    @region('.app__nav')
    nav;

    onRender() {
        this.nav.show(new SideNav());
        const dataPromise = this.loadData();
        if(dataPromise) {
            dataPromise.then(() => {
                this.content.show(this.getContentView());
                this.trigger('load');
            });
        } else {
            this.content.show(this.getContentView());
        }
    }

    loadData() {}

    getContentView() {
        throw new Error('attempt to call abstract method');
    }

    onRouteUpdate() {}
}

export default AppLayout;
