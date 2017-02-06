import './styles.css';
import d3 from 'd3';
import highlight from '../../util/highlight';
import {ItemView} from 'backbone.marionette';
import $ from 'jquery';
import router from '../../router';
import {className, on, behavior} from '../../decorators';
import attachmentType from '../../util/attachmentType';
import template from './AttachmentView.hbs';

@className('attachment')
@behavior('TooltipBehavior', {position: 'bottom'})
class AttachmentView extends ItemView {
    template = template;

    initialize({attachment}) {
        this.attachment = attachment;
        this.type = attachmentType(this.attachment.type);
        this.sourceUrl = 'data/' + this.attachment.source;
    }

    onRender() {
        if(this.needsFetch() && !this.content) {
            this.loadContent().then(this.render);
        } else if(this.type === 'code') {
            const codeBlock = this.$('.attachment__code');
            codeBlock.addClass('language-' + this.attachment.type.split('/').pop());
            highlight.highlightBlock(codeBlock[0]);
        }
    }

    @on('click .attachment__media')
    onImageClick() {
        const expanded = router.getUrlParams().expanded === 'true' ? null : true;
        router.setSearch({expanded});
    }

    loadContent() {
        return $.ajax(this.sourceUrl, {dataType: 'text'}).then((responseText) => {
            if(this.type === 'csv' || this.type === 'tab-separated-values') {
                this.isTable = true;
                this.content = (this.type === 'csv' ? d3.csv : d3.tsv).parseRows(responseText);
            } else if(this.type === 'uri') {
                this.content = responseText.split('\n')
                    .map(line => line.trim())
                    .filter(line => line.length > 0)
                    .map(line => ({
                         comment: line.startsWith('#'),
                         text: line
                    }));
            } else {
                this.content = responseText;
            }
        });
    }

    needsFetch() {
        if(this.isTable) {
            return true;
        }
        return ['text', 'code', 'uri'].indexOf(this.type) > -1;
    }

    serializeData() {
        return {
            type: this.type,
            content: this.content,
            sourceUrl: this.sourceUrl,
            attachment: this.attachment,
            isTable: this.isTable,
            route: {
                baseUrl: this.options.baseUrl
            }
        };
    }
}

export default AttachmentView;
