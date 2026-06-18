package gg.deepsite.pewpew.api.objects.attachment;

import gg.deepsite.pewpew.api.enums.AttachmentType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DefaultAttachment {

    private AttachmentType slot;
    private String attachmentId;
    private boolean forced;
}
